package com.budgetpro.infrastructure.rest.compra.advice;

import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ControllerAdvice para manejo centralizado de excepciones relacionadas con Compras.
 * 
 * Captura excepciones de negocio y de validación, y las convierte a respuestas HTTP apropiadas.
 */
@RestControllerAdvice(basePackages = "com.budgetpro.infrastructure.rest.compra")
public class CompraControllerAdvice {

    /**
     * Maneja la excepción de saldo insuficiente.
     * Retorna código HTTP 409 CONFLICT con un cuerpo de error JSON estándar.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción de saldo insuficiente
     * @return ResponseEntity con código 409 y mensaje de error
     */
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "CONFLICT",
            ex.getMessage(),
            getTraceId()
        );
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Maneja errores de validación del request (MethodArgumentNotValidException).
     * Retorna código HTTP 400 BAD REQUEST listando campo y error.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción de validación
     * @return ResponseEntity con código 400 y lista de errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse error = new ValidationErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "BAD_REQUEST",
            fieldErrors,
            getTraceId()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja errores de IllegalArgumentException (validaciones de negocio adicionales).
     * Retorna código HTTP 400 BAD REQUEST.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción de argumento inválido
     * @return ResponseEntity con código 400 y mensaje de error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "BAD_REQUEST",
            ex.getMessage(),
            getTraceId()
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Maneja errores de IllegalStateException (invariantes de negocio, concurrencia, recursos no encontrados).
     * Retorna código HTTP 409 CONFLICT.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción de estado inválido
     * @return ResponseEntity con código 409 y mensaje de error
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "CONFLICT",
            ex.getMessage(),
            getTraceId()
        );
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Maneja errores de OptimisticLockingFailureException (concurrencia optimista).
     * Retorna código HTTP 409 CONFLICT.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción de locking optimista
     * @return ResponseEntity con código 409 y mensaje de error
     */
    @ExceptionHandler({OptimisticLockingFailureException.class, org.springframework.dao.OptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(OptimisticLockingFailureException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "CONFLICT",
            "La compra fue modificada por otro proceso. Por favor, actualiza y vuelve a intentar.",
            getTraceId()
        );
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Maneja errores de acceso a datos (DataAccessException) y timeouts de base de datos.
     * Retorna código HTTP 503 SERVICE UNAVAILABLE.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción de acceso a datos
     * @return ResponseEntity con código 503 y mensaje de error
     */
    @ExceptionHandler({DataAccessException.class, QueryTimeoutException.class, SQLTimeoutException.class})
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "SERVICE_UNAVAILABLE",
            "El servicio no está disponible temporalmente. Por favor, inténtalo de nuevo más tarde.",
            getTraceId()
        );
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }

    /**
     * Maneja cualquier otra excepción no manejada (errores inesperados).
     * Retorna código HTTP 500 INTERNAL SERVER ERROR.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción no manejada
     * @return ResponseEntity con código 500 y mensaje de error genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo más tarde.",
            getTraceId()
        );
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    /**
     * DTO para respuestas de error genéricas.
     * Formato JSON estándar: timestamp, status, error, message, traceId.
     * El traceId permite al frontend reportar errores con referencia exacta para debugging.
     */
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String traceId
    ) {
    }

    /**
     * DTO para respuestas de error de validación.
     * Lista campo y error para cada campo inválido.
     * Incluye traceId para correlacionar errores con logs del servidor.
     */
    public record ValidationErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            Map<String, String> fieldErrors,
            String traceId
    ) {
    }

    /**
     * Obtiene el traceId (correlationId) del MDC para incluirlo en las respuestas de error.
     * Si no existe en el MDC (por ejemplo, si el filtro no se ejecutó), retorna null.
     * 
     * @return El traceId del MDC o null si no existe
     */
    private String getTraceId() {
        return MDC.get("correlationId");
    }
}
