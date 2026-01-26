package com.budgetpro.infrastructure.rest.recurso.advice;

import com.budgetpro.application.recurso.exception.RecursoDuplicadoException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ControllerAdvice para manejo centralizado de excepciones relacionadas con Recursos.
 * 
 * Captura excepciones de negocio y de validación, y las convierte a respuestas HTTP apropiadas.
 */
@RestControllerAdvice(basePackages = "com.budgetpro.infrastructure.rest.recurso")
public class RecursoControllerAdvice {

    /**
     * Maneja la excepción de recurso duplicado.
     * Retorna código HTTP 409 CONFLICT con un cuerpo de error JSON estándar.
     * Incluye el traceId (correlationId) del MDC para que el frontend pueda reportar errores con referencia exacta.
     * 
     * @param ex La excepción de recurso duplicado
     * @return ResponseEntity con código 409 y mensaje de error
     */
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoDuplicado(RecursoDuplicadoException ex) {
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
