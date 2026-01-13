package com.budgetpro.application.partida.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear una partida con un padreId que no existe.
 */
public class PartidaPadreNoEncontradaException extends RuntimeException {

    public PartidaPadreNoEncontradaException(UUID padreId) {
        super(String.format("No se encontró una partida padre con ID '%s'", padreId));
    }
}
