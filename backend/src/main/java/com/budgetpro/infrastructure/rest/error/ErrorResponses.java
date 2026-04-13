package com.budgetpro.infrastructure.rest.error;

import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public final class ErrorResponses {

    private ErrorResponses() {
    }

    public static ErrorResponse error(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, traceId(), null);
    }

    public static ErrorResponse errorWithDetails(int status, String error, String message, Map<String, Object> details) {
        Map<String, Object> safeDetails = details == null ? null : Collections.unmodifiableMap(details);
        return new ErrorResponse(LocalDateTime.now(), status, error, message, traceId(), safeDetails);
    }

    public static ValidationErrorResponse validation(int status, String error, Map<String, String> fieldErrors) {
        return new ValidationErrorResponse(LocalDateTime.now(), status, error, fieldErrors, traceId());
    }

    private static String traceId() {
        return MDC.get("correlationId");
    }

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String traceId,
            Map<String, Object> details
    ) {
    }

    public record ValidationErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            Map<String, String> fieldErrors,
            String traceId
    ) {
    }
}

