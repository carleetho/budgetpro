package com.budgetpro.infrastructure.rest.controller;

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

    @ExceptionHandler(com.budgetpro.application.compra.exception.BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleCompraBusinessRule(com.budgetpro.application.compra.exception.BusinessRuleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "BUSINESS_RULE_VIOLATION");
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(com.budgetpro.application.produccion.exception.BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleProduccionBusinessRule(com.budgetpro.application.produccion.exception.BusinessRuleException ex) {
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
        String message = ex.getMessage() != null ? ex.getMessage() : "";
        
        // Detectar si es una violación de regla de negocio por el contenido del mensaje
        // L-01, L-04, REGLA-153 son reglas de negocio que deben retornar 422
        if (message.contains("L-01") || message.contains("L-04") || message.contains("REGLA-153") 
            || message.contains("presupuesto insuficiente") || message.contains("proveedor inactivo")
            || message.contains("partida debe ser hoja") || message.contains("no puede exceder el presupuesto disponible")) {
            // Error de regla de negocio: 422 Unprocessable Entity
            body.put("message", message);
            body.put("error", "BUSINESS_RULE_VIOLATION");
            body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
        }
        
        // Otros IllegalArgumentException son errores de validación: 400 Bad Request
        body.put("message", message);
        body.put("error", "INVALID_ARGUMENT");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.OverDeliveryException.class)
    public ResponseEntity<Map<String, Object>> handleOverDelivery(com.budgetpro.application.compra.exception.OverDeliveryException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "OVER_DELIVERY");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.InvalidStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(com.budgetpro.application.compra.exception.InvalidStateException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "INVALID_STATE");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.MissingGuiaRemisionException.class)
    public ResponseEntity<Map<String, Object>> handleMissingGuiaRemision(com.budgetpro.application.compra.exception.MissingGuiaRemisionException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "MISSING_GUIA_REMISION");
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.UnauthorizedRoleException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedRole(com.budgetpro.application.compra.exception.UnauthorizedRoleException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "UNAUTHORIZED_ROLE");
        body.put("status", HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.DuplicateReceptionException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateReception(com.budgetpro.application.compra.exception.DuplicateReceptionException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "DUPLICATE_RECEPTION");
        body.put("status", HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.ProjectNotActiveException.class)
    public ResponseEntity<Map<String, Object>> handleProjectNotActive(com.budgetpro.application.compra.exception.ProjectNotActiveException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("error", "PROJECT_NOT_ACTIVE");
        body.put("status", HttpStatus.PRECONDITION_FAILED.value());
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(body);
    }
}
