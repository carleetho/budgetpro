package com.budgetpro.application.compra.exception;

/**
 * Excepción lanzada cuando se intenta crear una recepción con una guía de remisión
 * que ya existe para la misma compra (violación de idempotencia).
 */
public class DuplicateReceptionException extends RuntimeException {
    public DuplicateReceptionException(String message) {
        super(message);
    }
}
