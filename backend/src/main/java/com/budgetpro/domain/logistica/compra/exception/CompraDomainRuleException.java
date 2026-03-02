package com.budgetpro.domain.logistica.compra.exception;

/**
 * Excepción de regla de negocio del dominio de Compras.
 */
public class CompraDomainRuleException extends RuntimeException {

    public CompraDomainRuleException(String message) {
        super(message);
    }

    public CompraDomainRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
