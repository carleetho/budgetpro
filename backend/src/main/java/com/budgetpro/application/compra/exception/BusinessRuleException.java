package com.budgetpro.application.compra.exception;

/**
 * Excepción de reglas de negocio para el módulo de Compras (LOG).
 * 
 * Se utiliza para violaciones de reglas de negocio como:
 * - L-01: Budget Check (presupuesto insuficiente)
 * - L-04: Provider Valid (proveedor inactivo)
 * - REGLA-153: Partida must be leaf node
 * 
 * Esta excepción debe ser manejada por GlobalExceptionHandler y retornar HTTP 422 Unprocessable Entity.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
