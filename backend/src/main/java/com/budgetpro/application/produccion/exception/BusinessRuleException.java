package com.budgetpro.application.produccion.exception;

/**
 * Excepción de reglas de negocio para el módulo de Producción (RPC).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
