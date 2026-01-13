package com.budgetpro.application.compra.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta registrar una compra con una partida que no existe.
 */
public class PartidaNoEncontradaException extends RuntimeException {

    public PartidaNoEncontradaException(UUID partidaId) {
        super(String.format("No se encontró una partida con ID '%s'", partidaId));
    }
}
