package com.budgetpro.infrastructure.rest.controller;

import com.budgetpro.application.finanzas.evm.exception.PeriodoFechaInvalidaException;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.infrastructure.rest.error.ErrorResponses;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.Map;

/**
 * Manejador global de excepciones REST.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.budgetpro.domain.logistica.compra.exception.CompraDomainRuleException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleCompraDomainRule(
            com.budgetpro.domain.logistica.compra.exception.CompraDomainRuleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponses.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), "BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.BusinessRuleException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleCompraBusinessRule(com.budgetpro.application.compra.exception.BusinessRuleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponses.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), "BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.produccion.exception.BusinessRuleException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleProduccionBusinessRule(com.budgetpro.application.produccion.exception.BusinessRuleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponses.error(HttpStatus.CONFLICT.value(), "BUSINESS_RULE", ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponses.error(HttpStatus.NOT_FOUND.value(), "NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(PeriodoFechaInvalidaException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handlePeriodoFechaInvalida(PeriodoFechaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponses.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), "INVALID_PERIOD", ex.getMessage()));
    }

    @ExceptionHandler(ProyectoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProyectoNotFound(ProyectoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Proyecto no encontrado",
                        "proyectoId", ex.getProyectoId().toString()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponses.error(HttpStatus.CONFLICT.value(), "ILLEGAL_STATE", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.error(HttpStatus.BAD_REQUEST.value(), "INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.OverDeliveryException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleOverDelivery(com.budgetpro.application.compra.exception.OverDeliveryException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.error(HttpStatus.BAD_REQUEST.value(), "OVER_DELIVERY", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.InvalidStateException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleInvalidState(com.budgetpro.application.compra.exception.InvalidStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.error(HttpStatus.BAD_REQUEST.value(), "INVALID_STATE", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.MissingGuiaRemisionException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleMissingGuiaRemision(com.budgetpro.application.compra.exception.MissingGuiaRemisionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponses.error(HttpStatus.BAD_REQUEST.value(), "MISSING_GUIA_REMISION", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.UnauthorizedRoleException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleUnauthorizedRole(com.budgetpro.application.compra.exception.UnauthorizedRoleException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponses.error(HttpStatus.FORBIDDEN.value(), "UNAUTHORIZED_ROLE", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.DuplicateReceptionException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleDuplicateReception(com.budgetpro.application.compra.exception.DuplicateReceptionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponses.error(HttpStatus.CONFLICT.value(), "DUPLICATE_RECEPTION", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.ProjectNotActiveException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleProjectNotActive(com.budgetpro.application.compra.exception.ProjectNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                .body(ErrorResponses.error(HttpStatus.PRECONDITION_FAILED.value(), "PROJECT_NOT_ACTIVE", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.rrhh.exception.ProyectoNoActivoException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleProyectoNoActivo(
            com.budgetpro.application.rrhh.exception.ProyectoNoActivoException ex) {
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                .body(ErrorResponses.error(HttpStatus.PRECONDITION_FAILED.value(), "PROYECTO_NO_ACTIVO", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.rrhh.exception.ConfiguracionLaboralNotFoundException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleConfiguracionLaboralNotFound(
            com.budgetpro.application.rrhh.exception.ConfiguracionLaboralNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponses.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), "CONFIGURACION_LABORAL_NO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler(com.budgetpro.application.compra.exception.AuthenticationRequiredException.class)
    public ResponseEntity<ErrorResponses.ErrorResponse> handleAuthenticationRequired(
            com.budgetpro.application.compra.exception.AuthenticationRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponses.error(HttpStatus.UNAUTHORIZED.value(), "AUTHENTICATION_REQUIRED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponses.ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new java.util.HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fe) {
                errors.put(fe.getField(), fe.getDefaultMessage());
            }
        });
        return ResponseEntity.badRequest()
                .body(ErrorResponses.validation(HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", errors));
    }
}
