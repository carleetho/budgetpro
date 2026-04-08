package com.budgetpro.application.rrhh.exception;

/**
 * REGLA-150: operaciones de RRHH que exigen un proyecto en ejecución ({@code ACTIVO}).
 */
public class ProyectoNoActivoException extends RuntimeException {

    public ProyectoNoActivoException(String message) {
        super(message);
    }
}
