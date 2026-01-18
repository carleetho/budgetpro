package com.budgetpro.application.proyecto.exception;

/**
 * Excepción lanzada cuando se intenta crear un proyecto con un nombre que ya existe.
 */
public class ProyectoDuplicadoException extends RuntimeException {

    public ProyectoDuplicadoException(String nombre) {
        super(String.format("Ya existe un proyecto con el nombre '%s'", nombre));
    }
}
