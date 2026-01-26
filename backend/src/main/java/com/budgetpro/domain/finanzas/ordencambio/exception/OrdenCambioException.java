package com.budgetpro.domain.finanzas.ordencambio.exception;

/**
 * Excepción de dominio para errores relacionados con Orden de Cambio.
 */
public class OrdenCambioException extends RuntimeException {

    public OrdenCambioException(String message) {
        super(message);
    }

    public OrdenCambioException(String message, Throwable cause) {
        super(message, cause);
    }
}
