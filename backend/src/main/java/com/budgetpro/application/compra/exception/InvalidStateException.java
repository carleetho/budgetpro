package com.budgetpro.application.compra.exception;

/**
 * Excepción lanzada cuando una compra no está en un estado válido
 * para realizar una operación (por ejemplo, recibir productos).
 */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String message) {
        super(message);
    }
}
