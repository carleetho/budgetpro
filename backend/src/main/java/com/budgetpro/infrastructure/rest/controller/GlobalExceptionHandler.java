package com.budgetpro.infrastructure.rest.controller;

import com.budgetpro.application.produccion.exception.BusinessRuleException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "BUSINESS_RULE");
        body.put("status", HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "NOT_FOUND");
        body.put("status", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        
        // Determinar si es un error de regla de negocio (422) o conflicto de estado (409)
        String message = ex.getMessage() != null ? ex.getMessage() : "";
        HttpStatus status;
        String errorCode;
        
        if (message.contains("L-01") || message.contains("L-04") || message.contains("REGLA-153") 
            || message.contains("presupuesto") || message.contains("proveedor") || message.contains("partida")) {
            // Error de regla de negocio: 422 Unprocessable Entity
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            errorCode = "BUSINESS_RULE_VIOLATION";
        } else {
            // Conflicto de estado: 409 Conflict
            status = HttpStatus.CONFLICT;
            errorCode = "ILLEGAL_STATE";
        }
        
        body.put("error", errorCode);
        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "INVALID_ARGUMENT");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
