package com.budgetpro.infrastructure.observability;

import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;
import com.budgetpro.domain.shared.port.out.ObservabilityPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Componente para logging estructurado de operaciones de integridad
 * criptográfica.
 * 
 * Todos los logs se emiten en formato estructurado para facilitar el análisis y
 * correlación en sistemas de agregación de logs.
 * 
 * Incluye correlation IDs para rastreo de requests y contexto completo para
 * análisis forense de violaciones de integridad.
 */
@Component
public class IntegrityEventLogger implements ObservabilityPort {

    private static final Logger log = LoggerFactory.getLogger(IntegrityEventLogger.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    /**
     * Registra la generación de un hash de integridad.
     *
     * @param correlationId ID de correlación para rastreo
     * @param presupuestoId ID del presupuesto
     * @param approvalHash  Hash de aprobación generado (truncado para logs)
     * @param executionHash Hash de ejecución generado (truncado para logs)
     * @param durationMs    Duración del cálculo en milisegundos
     * @param partidasCount Cantidad de partidas procesadas
     * @param algorithm     Versión del algoritmo usado
     */
    public void logHashGeneration(String correlationId, UUID presupuestoId, String approvalHash, String executionHash,
            long durationMs, int partidasCount, String algorithm) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "integrity-hash-service");
        logData.put("action", "hash_generated");
        logData.put("presupuesto_id", presupuestoId != null ? presupuestoId.toString() : null);
        logData.put("hash_approval", truncateHash(approvalHash));
        logData.put("hash_execution", truncateHash(executionHash));
        logData.put("duration_ms", durationMs);
        logData.put("partidas_count", partidasCount);
        logData.put("algorithm", algorithm);
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.info("Integrity hash generated: {}", logData);
        }
    }

    /**
     * Registra una validación de hash de integridad.
     *
     * @param correlationId ID de correlación
     * @param presupuestoId ID del presupuesto
     * @param success       true si la validación fue exitosa
     * @param durationMs    Duración de la validación en milisegundos
     * @param details       Detalles adicionales de la validación
     * @param algorithm     Versión del algoritmo usado
     */
    public void logHashValidation(String correlationId, UUID presupuestoId, boolean success, long durationMs,
            String details, String algorithm) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "integrity-hash-service");
        logData.put("action", "hash_validated");
        logData.put("presupuesto_id", presupuestoId != null ? presupuestoId.toString() : null);
        logData.put("validation_result", success ? "SUCCESS" : "FAILURE");
        logData.put("duration_ms", durationMs);
        logData.put("details", details);
        logData.put("algorithm", algorithm);
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            if (success) {
                log.info("Integrity validation successful: {}", logData);
            } else {
                log.warn("Integrity validation failed: {}", logData);
            }
        }
    }

    /**
     * Registra una violación de integridad detectada.
     * 
     * **CRITICAL LOG**: Este log debe ser monitoreado activamente ya que indica
     * posibles intentos de tampering o corrupción de datos.
     *
     * @param correlationId ID de correlación
     * @param exception     Excepción de violación de integridad
     * @param detectedBy    ID del usuario o sistema que detectó la violación
     * @param operation     Operación que estaba ejecutándose cuando se detectó
     * @param algorithm     Versión del algoritmo usado
     */
    public void logIntegrityViolation(String correlationId, BudgetIntegrityViolationException exception,
            UUID detectedBy, String operation, String algorithm) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "integrity-hash-service");
        logData.put("action", "integrity_violation");
        logData.put("presupuesto_id",
                exception.getPresupuestoId() != null ? exception.getPresupuestoId().getValue().toString() : null);
        logData.put("expected_hash", truncateHash(exception.getExpectedHash()));
        logData.put("actual_hash", truncateHash(exception.getActualHash()));
        logData.put("violation_type", exception.getViolationType());
        logData.put("detected_by", detectedBy != null ? detectedBy.toString() : null);
        logData.put("operation", operation);
        logData.put("algorithm", algorithm);
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.error("CRITICAL: Budget integrity violation detected: {}", logData, exception);
        }
    }

    /**
     * Trunca un hash para mostrarlo en logs (primeros 16 caracteres + "...")
     * 
     * @param hash Hash completo (64 caracteres hex)
     * @return Hash truncado para logs
     */
    private String truncateHash(String hash) {
        if (hash == null || hash.length() <= 16) {
            return hash;
        }
        return hash.substring(0, 16) + "...";
    }

    /**
     * Genera un correlation ID único para rastreo de requests.
     *
     * @return UUID como string
     */
    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void recordMetrics(String name, double value, String... tags) {
        log.debug("Metric recorded: name={}, value={}, tags={}", name, value, tags);
    }

    @Override
    public void logEvent(String event, String message) {
        log.info("Event: {}, Message: {}", event, message);
    }

    @Override
    public void logError(String correlationId, String source, String operation, String entityId, Exception e) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", source);
        logData.put("action", operation);
        logData.put("entity_id", entityId);
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.error("Error in operation {}: {}", operation, logData, e);
        }
    }

    @Override
    public void recordHashEvent(String correlationId, String type, UUID budgetId, String hash, long duration,
            int itemCount, String algorithm) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("component", "integrity-hash-service");
        logData.put("action", "hash_generated");
        logData.put("type", type);
        logData.put("presupuesto_id", budgetId != null ? budgetId.toString() : null);
        logData.put("hash", truncateHash(hash));
        logData.put("duration_ms", duration);
        logData.put("partidas_count", itemCount);
        logData.put("algorithm", algorithm);
        logData.put("correlation_id", correlationId);

        try (MDC.MDCCloseable ignored = MDC.putCloseable(CORRELATION_ID_KEY, correlationId)) {
            log.info("Integrity hash recorded: {}", logData);
        }
    }
}
