package com.budgetpro.application.compra.exception;

/**
 * Excepción lanzada cuando falta la guía de remisión en una recepción.
 * La guía de remisión es un requisito legal para la recepción de bienes físicos.
 */
public class MissingGuiaRemisionException extends RuntimeException {
    public MissingGuiaRemisionException(String message) {
        super(message);
    }
}
