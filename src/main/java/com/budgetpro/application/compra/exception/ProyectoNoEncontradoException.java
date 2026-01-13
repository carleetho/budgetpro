package com.budgetpro.application.compra.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta registrar una compra para un proyecto que no existe.
 */
public class ProyectoNoEncontradoException extends RuntimeException {

    public ProyectoNoEncontradoException(UUID proyectoId) {
        super(String.format("No se encontró un proyecto con ID '%s'", proyectoId));
    }
}
