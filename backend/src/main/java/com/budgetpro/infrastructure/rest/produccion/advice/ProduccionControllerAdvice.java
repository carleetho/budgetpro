package com.budgetpro.infrastructure.rest.produccion.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador de excepciones para endpoints de Producción (RPC).
 */
@RestControllerAdvice(basePackages = "com.budgetpro.infrastructure.rest.produccion")
public class ProduccionControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Error de validación en los datos enviados");
        body.put("error", "VALIDATION_ERROR");
        body.put("status", 400);

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }
}
