package com.budgetpro.domain.shared.port.out;

/**
 * Port for domain observability, decoupling domain from infrastructure metrics
 * and logging.
 */
public interface ObservabilityPort {
    void recordMetrics(String name, double value, String... tags);

    void logEvent(String event, String message);

    String generateCorrelationId();

    void logError(String correlationId, String source, String operation, String entityId, Exception e);

    void recordHashEvent(String correlationId, String type, java.util.UUID budgetId, String hash, long duration,
            int itemCount, String algorithm);
}
