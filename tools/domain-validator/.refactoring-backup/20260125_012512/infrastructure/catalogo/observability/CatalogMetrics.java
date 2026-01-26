package com.budgetpro.infrastructure.catalogo.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Componente para registrar métricas de operaciones de catálogo.
 * 
 * Métricas expuestas:
 * - catalog.api.requests.total: Contador de requests a APIs de catálogo
 * - catalog.api.latency: Tiempo de respuesta de APIs (p50, p95, p99)
 * - catalog.cache.requests: Contador de hits/misses de cache
 * - snapshot.creation.total: Contador de snapshots creados
 * - snapshot.creation.duration: Duración de creación de snapshots
 * - snapshot.rendimiento.overrides: Contador de modificaciones de rendimiento
 */
@Component
public class CatalogMetrics {

    private static final String METRIC_CATALOG_API_REQUESTS = "catalog.api.requests.total";
    private static final String METRIC_CATALOG_API_LATENCY = "catalog.api.latency";
    private static final String METRIC_CATALOG_CACHE_REQUESTS = "catalog.cache.requests";
    private static final String METRIC_SNAPSHOT_CREATION_TOTAL = "snapshot.creation.total";
    private static final String METRIC_SNAPSHOT_CREATION_DURATION = "snapshot.creation.duration";
    private static final String METRIC_SNAPSHOT_RENDIMIENTO_OVERRIDES = "snapshot.rendimiento.overrides";

    private final MeterRegistry registry;

    public CatalogMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Registra una llamada a la API de catálogo.
     *
     * @param catalogSource Fuente del catálogo (ej: "CAPECO", "MOCK")
     * @param operation Operación realizada (ej: "fetchRecurso", "fetchAPU", "searchRecursos")
     * @param durationMs Duración de la operación en milisegundos
     * @param success true si la operación fue exitosa, false si hubo error
     */
    public void recordApiCall(String catalogSource, String operation, long durationMs, boolean success) {
        // Contador de requests
        registry.counter(
                METRIC_CATALOG_API_REQUESTS,
                "source", catalogSource,
                "operation", operation,
                "status", success ? "success" : "error"
        ).increment();

        // Timer para latencia (p50, p95, p99)
        registry.timer(
                METRIC_CATALOG_API_LATENCY,
                "source", catalogSource,
                "operation", operation
        ).record(Duration.ofMillis(durationMs));
    }

    /**
     * Registra un acceso al cache (hit o miss).
     *
     * @param catalogSource Fuente del catálogo
     * @param hit true si fue cache hit, false si fue miss
     */
    public void recordCacheHit(String catalogSource, boolean hit) {
        registry.counter(
                METRIC_CATALOG_CACHE_REQUESTS,
                "source", catalogSource,
                "result", hit ? "hit" : "miss"
        ).increment();
    }

    /**
     * Registra la creación de un snapshot.
     *
     * @param durationMs Duración de la creación en milisegundos
     * @param catalogSource Fuente del catálogo
     * @param insumosCount Cantidad de insumos en el snapshot
     */
    public void recordSnapshotCreation(long durationMs, String catalogSource, int insumosCount) {
        registry.counter(
                METRIC_SNAPSHOT_CREATION_TOTAL,
                "source", catalogSource,
                "insumos_count", String.valueOf(insumosCount)
        ).increment();

        registry.timer(
                METRIC_SNAPSHOT_CREATION_DURATION,
                "source", catalogSource
        ).record(Duration.ofMillis(durationMs));
    }

    /**
     * Registra una modificación de rendimiento (override).
     *
     * @param catalogSource Fuente del catálogo
     */
    public void recordRendimientoOverride(String catalogSource) {
        registry.counter(
                METRIC_SNAPSHOT_RENDIMIENTO_OVERRIDES,
                "source", catalogSource
        ).increment();
    }

    /**
     * Registra un error en operación de catálogo.
     *
     * @param catalogSource Fuente del catálogo
     * @param operation Operación que falló
     * @param errorType Tipo de error (ej: "CatalogNotFoundException", "CatalogServiceException")
     */
    public void recordApiError(String catalogSource, String operation, String errorType) {
        registry.counter(
                METRIC_CATALOG_API_REQUESTS,
                "source", catalogSource,
                "operation", operation,
                "status", "error",
                "error_type", errorType
        ).increment();
    }
}
