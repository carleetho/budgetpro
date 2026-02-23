package com.budgetpro.application.compra.exception;

/**
 * Excepción lanzada cuando se requiere autenticación pero el usuario
 * no está autenticado o no se puede determinar su identidad.
 */
public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
