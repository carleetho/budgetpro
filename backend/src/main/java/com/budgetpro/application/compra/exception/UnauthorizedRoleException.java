package com.budgetpro.application.compra.exception;

/**
 * Excepción lanzada cuando un usuario no tiene el rol requerido
 * para realizar una operación (por ejemplo, RESIDENTE para recibir órdenes).
 */
public class UnauthorizedRoleException extends RuntimeException {
    public UnauthorizedRoleException(String message) {
        super(message);
    }
}
