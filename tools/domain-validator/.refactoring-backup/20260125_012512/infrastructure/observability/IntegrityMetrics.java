package com.budgetpro.infrastructure.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Componente para registrar métricas de operaciones de integridad criptográfica.
 * 
 * Métricas expuestas:
 * - budget.integrity.hash_generated.total: Contador de hashes generados
 * - budget.integrity.hash_calculation.duration: Duración de cálculo de hash (p50, p95, p99)
 * - budget.integrity.hash_validated.total: Contador de validaciones (success/failure)
 * - budget.integrity.validation.duration: Duración de validación (p50, p95, p99)
 * - budget.integrity.violations.total: Contador de violaciones de integridad (CRITICAL)
 * - budget.integrity.partidas_count: Gauge con cantidad de partidas procesadas
 * 
 * Todas las métricas están etiquetadas con:
 * - operation: Tipo de operación (approval_hash, execution_hash, validation)
 * - algorithm: Versión del algoritmo (SHA-256-v1)
 */
@Component
public class IntegrityMetrics {

    private static final String METRIC_HASH_GENERATED_TOTAL = "budget.integrity.hash_generated.total";
    private static final String METRIC_HASH_CALCULATION_DURATION = "budget.integrity.hash_calculation.duration";
    private static final String METRIC_HASH_VALIDATED_TOTAL = "budget.integrity.hash_validated.total";
    private static final String METRIC_VALIDATION_DURATION = "budget.integrity.validation.duration";
    private static final String METRIC_VIOLATIONS_TOTAL = "budget.integrity.violations.total";
    private static final String METRIC_PARTIDAS_COUNT = "budget.integrity.partidas_count";

    private final MeterRegistry registry;

    public IntegrityMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Registra la generación de un hash de integridad.
     *
     * @param durationMs Duración del cálculo en milisegundos
     * @param partidasCount Cantidad de partidas procesadas
     * @param operation Tipo de operación ("approval_hash" o "execution_hash")
     * @param algorithm Versión del algoritmo (ej: "SHA-256-v1")
     */
    public void recordHashGeneration(long durationMs, int partidasCount, String operation, String algorithm) {
        // Contador de hashes generados
        registry.counter(
                METRIC_HASH_GENERATED_TOTAL,
                "operation", operation,
                "algorithm", algorithm
        ).increment();

        // Timer para duración de cálculo (p50, p95, p99)
        registry.timer(
                METRIC_HASH_CALCULATION_DURATION,
                "operation", operation,
                "algorithm", algorithm
        ).record(Duration.ofMillis(durationMs));

        // Gauge para cantidad de partidas procesadas
        registry.gauge(
                METRIC_PARTIDAS_COUNT,
                io.micrometer.core.instrument.Tags.of(
                        "operation", operation,
                        "algorithm", algorithm
                ),
                partidasCount,
                Integer::doubleValue
        );
    }

    /**
     * Registra una validación de hash de integridad.
     *
     * @param success true si la validación fue exitosa, false si falló
     * @param durationMs Duración de la validación en milisegundos
     * @param algorithm Versión del algoritmo usado
     */
    public void recordHashValidation(boolean success, long durationMs, String algorithm) {
        // Contador de validaciones con status
        registry.counter(
                METRIC_HASH_VALIDATED_TOTAL,
                "status", success ? "success" : "failure",
                "algorithm", algorithm
        ).increment();

        // Timer para duración de validación (p50, p95, p99)
        registry.timer(
                METRIC_VALIDATION_DURATION,
                "status", success ? "success" : "failure",
                "algorithm", algorithm
        ).record(Duration.ofMillis(durationMs));
    }

    /**
     * Registra una violación de integridad detectada.
     * 
     * **CRITICAL METRIC**: Esta métrica debe ser monitoreada activamente
     * ya que indica posibles intentos de tampering o corrupción de datos.
     *
     * @param violationType Tipo de violación (ej: "Tampering detected: Approval hash mismatch")
     * @param algorithm Versión del algoritmo usado
     */
    public void recordIntegrityViolation(String violationType, String algorithm) {
        registry.counter(
                METRIC_VIOLATIONS_TOTAL,
                "violation_type", violationType != null ? violationType : "unknown",
                "algorithm", algorithm
        ).increment();
    }
}
