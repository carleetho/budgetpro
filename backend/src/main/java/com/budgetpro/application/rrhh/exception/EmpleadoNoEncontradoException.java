package com.budgetpro.application.rrhh.exception;

public class EmpleadoNoEncontradoException extends RuntimeException {
    public EmpleadoNoEncontradoException(String id) {
        super("No se encontró el empleado con ID: " + id);
    }
}
