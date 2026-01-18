package com.budgetpro.application.presupuesto.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear un presupuesto para un proyecto que no existe.
 */
public class ProyectoNoEncontradoException extends RuntimeException {

    public ProyectoNoEncontradoException(UUID proyectoId) {
        super(String.format("No se encontró un proyecto con ID '%s'", proyectoId));
    }
}
