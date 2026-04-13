package com.budgetpro.infrastructure.rest.recurso.advice;

import com.budgetpro.application.recurso.exception.RecursoDuplicadoException;
import com.budgetpro.infrastructure.rest.error.ErrorResponses;
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
    public ResponseEntity<ErrorResponses.ErrorResponse> handleRecursoDuplicado(RecursoDuplicadoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponses.error(HttpStatus.CONFLICT.value(), "CONFLICT", ex.getMessage()));
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
    public ResponseEntity<ErrorResponses.ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.validation(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", fieldErrors));
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
    public ResponseEntity<ErrorResponses.ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.error(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST", ex.getMessage()));
    }
}
