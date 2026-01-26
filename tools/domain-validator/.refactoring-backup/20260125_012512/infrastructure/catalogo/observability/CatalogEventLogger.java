package com.budgetpro.infrastructure.catalogo.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Componente para logging estructurado de operaciones de catálogo.
 * 
 * Todos los logs se emiten en formato estructurado para facilitar
 * el análisis y correlación en sistemas de agregación de logs.
 * 
 * Incluye correlation IDs para rastreo de requests.
 */
@Component
public class CatalogEventLogger {

    private static final Logger log = LoggerFactory.getLogger(CatalogEventLogger.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    /**
     * Registra una llamada a la API de catálogo.
     *
     * @param correlationId ID de correlación para rastreo
     * @param catalogSource Fuente del catálogo
     * @param operation Operación realizada
     * @param externalId ID externo del recurso/APU
     * @param durationMs Duración en milisegundos
     * @param success true si fue exitosa
     * @param error Excepción si hubo error (null si fue exitosa)
     */
    public void logApiCall(String correlationId, String catalogSource, String operation,
                          String externalId, long durationMs, boolean success, Exception error) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "catalog-adapter");
        logData.put("action", operation);
        logData.put("catalog_source", catalogSource);
        logData.put("external_id", externalId);
        logData.put("duration_ms", durationMs);
        logData.put("status", success ? "success" : "error");
        logData.put("correlation_id", correlationId);

        if (error != null) {
            logData.put("error_type", error.getClass().getSimpleName());
            logData.put("error_message", error.getMessage());
        }

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            if (success) {
                log.info("Catalog API call completed: {}", logData);
            } else {
                log.error("Catalog API call failed: {}", logData, error);
            }
        }
    }

    /**
     * Registra un acceso al cache.
     *
     * @param correlationId ID de correlación
     * @param catalogSource Fuente del catálogo
     * @param externalId ID externo
     * @param hit true si fue cache hit
     */
    public void logCacheAccess(String correlationId, String catalogSource, String externalId, boolean hit) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "catalog-cache");
        logData.put("action", "cache_access");
        logData.put("catalog_source", catalogSource);
        logData.put("external_id", externalId);
        logData.put("result", hit ? "hit" : "miss");
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.debug("Cache access: {}", logData);
        }
    }

    /**
     * Registra la creación de un snapshot.
     *
     * @param correlationId ID de correlación
     * @param apuSnapshotId ID del snapshot creado
     * @param externalApuId ID externo del APU
     * @param catalogSource Fuente del catálogo
     * @param durationMs Duración en milisegundos
     * @param insumosCount Cantidad de insumos
     */
    public void logSnapshotCreation(String correlationId, UUID apuSnapshotId, String externalApuId,
                                   String catalogSource, long durationMs, int insumosCount) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "snapshot-service");
        logData.put("action", "create_snapshot");
        logData.put("apu_snapshot_id", apuSnapshotId != null ? apuSnapshotId.toString() : null);
        logData.put("external_apu_id", externalApuId);
        logData.put("catalog_source", catalogSource);
        logData.put("duration_ms", durationMs);
        logData.put("insumos_count", insumosCount);
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.info("APU snapshot created: {}", logData);
        }
    }

    /**
     * Registra una modificación de rendimiento.
     *
     * @param correlationId ID de correlación
     * @param apuSnapshotId ID del snapshot
     * @param rendimientoOriginal Rendimiento original del catálogo
     * @param rendimientoAnterior Rendimiento anterior (antes de la modificación)
     * @param rendimientoNuevo Nuevo rendimiento
     * @param usuarioId ID del usuario que realizó la modificación
     */
    public void logRendimientoModification(String correlationId, UUID apuSnapshotId,
                                          BigDecimal rendimientoOriginal, BigDecimal rendimientoAnterior,
                                          BigDecimal rendimientoNuevo, UUID usuarioId) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "snapshot-service");
        logData.put("action", "update_rendimiento");
        logData.put("apu_snapshot_id", apuSnapshotId != null ? apuSnapshotId.toString() : null);
        logData.put("rendimiento_original", rendimientoOriginal != null ? rendimientoOriginal.toString() : null);
        logData.put("rendimiento_anterior", rendimientoAnterior != null ? rendimientoAnterior.toString() : null);
        logData.put("rendimiento_nuevo", rendimientoNuevo != null ? rendimientoNuevo.toString() : null);
        logData.put("modificado_por", usuarioId != null ? usuarioId.toString() : null);
        logData.put("correlation_id", correlationId);

        // Calcular desviación
        if (rendimientoOriginal != null && rendimientoNuevo != null) {
            BigDecimal desviacion = rendimientoNuevo.subtract(rendimientoOriginal);
            logData.put("desviacion_original", desviacion.toString());
        }
        if (rendimientoAnterior != null && rendimientoNuevo != null) {
            BigDecimal cambio = rendimientoNuevo.subtract(rendimientoAnterior);
            logData.put("cambio_absoluto", cambio.toString());
        }

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.info("Rendimiento modified: {}", logData);
        }
    }

    /**
     * Registra un error en operación de catálogo.
     *
     * @param correlationId ID de correlación
     * @param catalogSource Fuente del catálogo
     * @param operation Operación que falló
     * @param externalId ID externo
     * @param error Excepción ocurrida
     */
    public void logCatalogError(String correlationId, String catalogSource, String operation,
                               String externalId, Exception error) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "catalog-adapter");
        logData.put("action", operation);
        logData.put("catalog_source", catalogSource);
        logData.put("external_id", externalId);
        logData.put("error_type", error != null ? error.getClass().getSimpleName() : "Unknown");
        logData.put("error_message", error != null ? error.getMessage() : "Unknown error");
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.error("Catalog operation failed: {}", logData, error);
        }
    }

    /**
     * Genera un correlation ID único para rastreo de requests.
     *
     * @return UUID como string
     */
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
