package com.budgetpro.application.apu.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear un APU con un recurso que no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(UUID recursoId) {
        super(String.format("No se encontró un recurso con ID '%s'", recursoId));
    }
}
