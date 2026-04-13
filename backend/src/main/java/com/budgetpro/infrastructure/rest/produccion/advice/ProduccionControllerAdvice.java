package com.budgetpro.infrastructure.rest.produccion.advice;

import com.budgetpro.infrastructure.rest.error.ErrorResponses;
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
    public ResponseEntity<ErrorResponses.ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(ErrorResponses.validation(400, "VALIDATION_ERROR", errors));
    }
}
