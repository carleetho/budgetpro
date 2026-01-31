package com.budgetpro.infrastructure.catalogo.adapter;

import com.budgetpro.domain.catalogo.exception.CatalogNotFoundException;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.model.RecursoSearchCriteria;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.infrastructure.catalogo.adapter.dto.CapecoApuResponse;
import com.budgetpro.infrastructure.catalogo.adapter.dto.CapecoRecursoResponse;
import com.budgetpro.infrastructure.catalogo.cache.CatalogCache;
import com.budgetpro.infrastructure.catalogo.observability.CatalogEventLogger;
import com.budgetpro.infrastructure.catalogo.observability.CatalogMetrics;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Adaptador de catálogo CAPECO con patrones de resiliencia.
 * 
 * Circuit Breaker: Se abre después de 5 fallos consecutivos Retry: Hasta 3
 * intentos con backoff exponencial Timeout: 5 segundos máximo Rate Limiter: 100
 * requests/minuto Fallback: Usa cache cuando el circuito está abierto
 */
@Component
@Profile("!test")
public class CapecoApiAdapter implements CatalogPort {

    private static final Logger log = LoggerFactory.getLogger(CapecoApiAdapter.class);
    private static final UUID EMPTY_PARTIDA_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 200L;

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private final String apiKey;
    private final CatalogCache catalogCache;
    private final CatalogMetrics catalogMetrics;
    private final CatalogEventLogger catalogEventLogger;

    public CapecoApiAdapter(RestTemplate restTemplate, @Value("${catalog.capeco.url}") String apiBaseUrl,
            @Value("${CAPECO_API_KEY}") String apiKey, CatalogCache catalogCache, CatalogMetrics catalogMetrics,
            CatalogEventLogger catalogEventLogger) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
        this.catalogCache = catalogCache;
        this.catalogMetrics = catalogMetrics;
        this.catalogEventLogger = catalogEventLogger;
    }

    @Override
    @CircuitBreaker(name = "catalog-api", fallbackMethod = "fetchRecursoFromCache")
    @Retry(name = "catalog-api")
    @TimeLimiter(name = "catalog-api")
    @RateLimiter(name = "catalog-api")
    public RecursoSnapshot fetchRecurso(String externalId, String catalogSource) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        long startTime = System.currentTimeMillis();
        boolean success = false;
        Exception error = null;

        try {
            log.debug("Fetching recurso {} from catalog {}", externalId, catalogSource);
            String url = String.format("%s/recursos/%s", apiBaseUrl, externalId);
            ResponseEntity<CapecoRecursoResponse> response = executeWithRetry(url, CapecoRecursoResponse.class,
                    externalId, catalogSource);
            CapecoRecursoResponse body = requireBody(response, externalId, catalogSource);
            RecursoSnapshot snapshot = mapToRecursoSnapshot(body, catalogSource);

            // Guardar en cache L2 para fallback futuro
            String cacheKey = String.format("%s:%s", catalogSource, externalId);
            catalogCache.putRecursoL2(cacheKey, snapshot);

            success = true;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "fetchRecurso", durationMs, true);
            catalogEventLogger.logApiCall(correlationId, catalogSource, "fetchRecurso", externalId, durationMs, true,
                    null);

            return snapshot;
        } catch (Exception e) {
            error = e;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "fetchRecurso", durationMs, false);
            catalogMetrics.recordApiError(catalogSource, "fetchRecurso", e.getClass().getSimpleName());
            catalogEventLogger.logApiCall(correlationId, catalogSource, "fetchRecurso", externalId, durationMs, false,
                    e);
            throw e;
        }
    }

    /**
     * Fallback: Intenta obtener desde cache cuando el circuito está abierto o hay
     * error.
     */
    private RecursoSnapshot fetchRecursoFromCache(String externalId, String catalogSource, Exception e) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        log.warn("Circuit breaker open or error, falling back to cache for recurso {} from {}", externalId,
                catalogSource, e);
        String cacheKey = String.format("%s:%s", catalogSource, externalId);
        boolean cacheHit = catalogCache.getRecursoL2(cacheKey).isPresent();
        catalogMetrics.recordCacheHit(catalogSource, cacheHit);
        catalogEventLogger.logCacheAccess(correlationId, catalogSource, externalId, cacheHit);

        return catalogCache.getRecursoL2(cacheKey).orElseThrow(() -> new CatalogServiceException(catalogSource,
                String.format("Catalog unavailable and no cache for recurso %s", externalId), e));
    }

    @Override
    @CircuitBreaker(name = "catalog-api", fallbackMethod = "searchRecursosFromCache")
    @Retry(name = "catalog-api")
    @TimeLimiter(name = "catalog-api")
    @RateLimiter(name = "catalog-api")
    public List<RecursoSnapshot> searchRecursos(RecursoSearchCriteria criteria, String catalogSource) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        long startTime = System.currentTimeMillis();
        boolean success = false;
        Exception error = null;

        try {
            log.debug("Searching recursos in catalog {} with criteria {}", catalogSource, criteria);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/recursos")
                    .queryParam("catalogSource", catalogSource);
            if (criteria != null) {
                if (criteria.getQuery() != null) {
                    builder.queryParam("query", criteria.getQuery());
                }
                if (criteria.getTipo() != null) {
                    builder.queryParam("tipo", criteria.getTipo().name());
                }
                if (criteria.getUnidad() != null) {
                    builder.queryParam("unidad", criteria.getUnidad());
                }
                if (criteria.getLimit() != null) {
                    builder.queryParam("limit", criteria.getLimit());
                }
                if (criteria.getOffset() != null) {
                    builder.queryParam("offset", criteria.getOffset());
                }
            }

            String url = builder.toUriString();
            ResponseEntity<CapecoRecursoResponse[]> response = executeWithRetry(url, CapecoRecursoResponse[].class,
                    "SEARCH", catalogSource);
            CapecoRecursoResponse[] body = requireBodyArray(response, "SEARCH", catalogSource);
            List<RecursoSnapshot> resultados = List.of(body).stream()
                    .map(item -> mapToRecursoSnapshot(item, catalogSource)).collect(Collectors.toList());

            success = true;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "searchRecursos", durationMs, true);
            catalogEventLogger.logApiCall(correlationId, catalogSource, "searchRecursos", "SEARCH", durationMs, true,
                    null);

            return resultados;
        } catch (Exception e) {
            error = e;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "searchRecursos", durationMs, false);
            catalogMetrics.recordApiError(catalogSource, "searchRecursos", e.getClass().getSimpleName());
            catalogEventLogger.logApiCall(correlationId, catalogSource, "searchRecursos", "SEARCH", durationMs, false,
                    e);
            throw e;
        }
    }

    /**
     * Fallback: Retorna lista vacía cuando el circuito está abierto (búsquedas no
     * se cachean).
     */
    private List<RecursoSnapshot> searchRecursosFromCache(RecursoSearchCriteria criteria, String catalogSource,
            Exception e) {
        log.warn("Circuit breaker open or error, returning empty list for search in {}", catalogSource, e);
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = "catalog-api", fallbackMethod = "fetchAPUFromCache")
    @Retry(name = "catalog-api")
    @TimeLimiter(name = "catalog-api")
    @RateLimiter(name = "catalog-api")
    public APUSnapshot fetchAPU(String externalApuId, String catalogSource) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        long startTime = System.currentTimeMillis();
        boolean success = false;
        Exception error = null;

        try {
            log.debug("Fetching APU {} from catalog {}", externalApuId, catalogSource);
            String url = String.format("%s/apus/%s", apiBaseUrl, externalApuId);
            ResponseEntity<CapecoApuResponse> response = executeWithRetry(url, CapecoApuResponse.class, externalApuId,
                    catalogSource);
            CapecoApuResponse body = requireBody(response, externalApuId, catalogSource);

            APUSnapshot snapshot = APUSnapshot.crear(APUSnapshotId.generate(), EMPTY_PARTIDA_ID, externalApuId,
                    catalogSource, body.getRendimiento() != null ? body.getRendimiento() : BigDecimal.ONE,
                    body.getUnidad(), LocalDateTime.now());

            if (body.getInsumos() != null) {
                for (CapecoApuResponse.CapecoApuInsumoResponse insumo : body.getInsumos()) {
                    APUInsumoSnapshot insumoSnapshot = APUInsumoSnapshot.crear(APUInsumoSnapshotId.generate(),
                            insumo.getRecursoExternalId(), insumo.getRecursoNombre(), insumo.getCantidad(),
                            insumo.getPrecioUnitario());
                    snapshot.agregarInsumo(insumoSnapshot);
                }
            }

            // Guardar en cache L2 para fallback futuro
            String cacheKey = String.format("%s:%s", catalogSource, externalApuId);
            catalogCache.putApuL2(cacheKey, snapshot);

            success = true;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "fetchAPU", durationMs, true);
            catalogEventLogger.logApiCall(correlationId, catalogSource, "fetchAPU", externalApuId, durationMs, true,
                    null);

            return snapshot;
        } catch (Exception e) {
            error = e;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "fetchAPU", durationMs, false);
            catalogMetrics.recordApiError(catalogSource, "fetchAPU", e.getClass().getSimpleName());
            catalogEventLogger.logApiCall(correlationId, catalogSource, "fetchAPU", externalApuId, durationMs, false,
                    e);
            throw e;
        }
    }

    /**
     * Fallback: Intenta obtener desde cache cuando el circuito está abierto o hay
     * error.
     */
    private APUSnapshot fetchAPUFromCache(String externalApuId, String catalogSource, Exception e) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        log.warn("Circuit breaker open or error, falling back to cache for APU {} from {}", externalApuId,
                catalogSource, e);
        String cacheKey = String.format("%s:%s", catalogSource, externalApuId);
        boolean cacheHit = catalogCache.getApuL2(cacheKey).isPresent();
        catalogMetrics.recordCacheHit(catalogSource, cacheHit);
        catalogEventLogger.logCacheAccess(correlationId, catalogSource, externalApuId, cacheHit);

        return catalogCache.getApuL2(cacheKey).orElseThrow(() -> new CatalogServiceException(catalogSource,
                String.format("Catalog unavailable and no cache for APU %s", externalApuId), e));
    }

    @Override
    @CircuitBreaker(name = "catalog-api", fallbackMethod = "isRecursoActiveFromCache")
    @Retry(name = "catalog-api")
    @TimeLimiter(name = "catalog-api")
    @RateLimiter(name = "catalog-api")
    public boolean isRecursoActive(String externalId, String catalogSource) {
        String correlationId = catalogEventLogger.generateCorrelationId();
        long startTime = System.currentTimeMillis();
        boolean success = false;
        Exception error = null;

        try {
            String url = String.format("%s/recursos/%s", apiBaseUrl, externalId);
            ResponseEntity<CapecoRecursoResponse> response = executeWithRetry(url, CapecoRecursoResponse.class,
                    externalId, catalogSource);
            CapecoRecursoResponse body = requireBody(response, externalId, catalogSource);
            boolean activo = body.getActivo() == null || body.getActivo();

            success = true;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "isRecursoActive", durationMs, true);
            catalogEventLogger.logApiCall(correlationId, catalogSource, "isRecursoActive", externalId, durationMs, true,
                    null);

            return activo;
        } catch (CatalogNotFoundException e) {
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "isRecursoActive", durationMs, true); // NotFound no es error
                                                                                              // funcional
            catalogEventLogger.logApiCall(correlationId, catalogSource, "isRecursoActive", externalId, durationMs, true,
                    null);
            return false;
        } catch (Exception e) {
            error = e;
            long durationMs = System.currentTimeMillis() - startTime;
            catalogMetrics.recordApiCall(catalogSource, "isRecursoActive", durationMs, false);
            catalogMetrics.recordApiError(catalogSource, "isRecursoActive", e.getClass().getSimpleName());
            catalogEventLogger.logApiCall(correlationId, catalogSource, "isRecursoActive", externalId, durationMs,
                    false, e);
            throw e;
        }
    }

    /**
     * Fallback: Si hay cache, asume activo. Si no, retorna false.
     */
    private boolean isRecursoActiveFromCache(String externalId, String catalogSource, Exception e) {
        log.warn("Circuit breaker open or error, checking cache for recurso {} from {}", externalId, catalogSource, e);
        String cacheKey = String.format("%s:%s", catalogSource, externalId);
        return catalogCache.getRecursoL2(cacheKey).isPresent();
    }

    private <T> ResponseEntity<T> executeWithRetry(String url, Class<T> responseType, String externalId,
            String catalogSource) {
        int attempts = 0;
        while (true) {
            try {
                HttpEntity<Void> request = new HttpEntity<>(
                        java.util.Objects.requireNonNull(buildHeaders(), "Headers no pueden ser nulos"));
                return restTemplate.exchange(java.util.Objects.requireNonNull(url, "URL no puede ser nula"),
                        java.util.Objects.requireNonNull(HttpMethod.GET, "HttpMethod no puede ser nulo"), request,
                        java.util.Objects.requireNonNull(responseType, "responseType no puede ser nulo"));
            } catch (HttpClientErrorException.NotFound e) {
                throw new CatalogNotFoundException(externalId, catalogSource);
            } catch (RestClientException e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new CatalogServiceException(catalogSource, "Error al consultar catalogo CAPECO", e);
                }
                backoff(attempts);
            }
        }
    }

    private void backoff(int attempts) {
        long waitMs = (long) (BASE_BACKOFF_MS * Math.pow(2, attempts - 1));
        try {
            TimeUnit.MILLISECONDS.sleep(waitMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CatalogServiceException("CAPECO", "Interrupcion durante reintentos", e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        return headers;
    }

    private RecursoSnapshot mapToRecursoSnapshot(CapecoRecursoResponse response, String catalogSource) {
        TipoRecurso tipo;
        try {
            tipo = TipoRecurso.valueOf(response.getTipo().toUpperCase());
        } catch (RuntimeException e) {
            throw new CatalogServiceException(catalogSource, "Tipo de recurso invalido: " + response.getTipo(), e);
        }

        return new RecursoSnapshot(response.getExternalId(), catalogSource, response.getNombre(), tipo,
                response.getUnidad(), response.getPrecio(), LocalDateTime.now());
    }

    private <T> T requireBody(ResponseEntity<T> response, String externalId, String catalogSource) {
        T body = response.getBody();
        if (body == null) {
            throw new CatalogServiceException(catalogSource, "Respuesta vacia para " + externalId);
        }
        return body;
    }

    private <T> T[] requireBodyArray(ResponseEntity<T[]> response, String externalId, String catalogSource) {
        T[] body = response.getBody();
        if (body == null) {
            throw new CatalogServiceException(catalogSource, "Respuesta vacia para " + externalId);
        }
        return body;
    }
}
