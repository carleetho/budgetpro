package com.budgetpro.application.rrhh.exception;

public class NumeroIdentificacionDuplicadoException extends RuntimeException {
    public NumeroIdentificacionDuplicadoException(String numeroIdentificacion) {
        super("Ya existe un empleado con el número de identificación: " + numeroIdentificacion);
    }
}
