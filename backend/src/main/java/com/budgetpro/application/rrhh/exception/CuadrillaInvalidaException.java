package com.budgetpro.application.rrhh.exception;

public class CuadrillaInvalidaException extends RuntimeException {

    public CuadrillaInvalidaException(String message) {
        super(message);
    }

    public CuadrillaInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
