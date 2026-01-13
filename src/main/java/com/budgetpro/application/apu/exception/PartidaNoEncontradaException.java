package com.budgetpro.application.apu.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear un APU para una partida que no existe.
 */
public class PartidaNoEncontradaException extends RuntimeException {

    public PartidaNoEncontradaException(UUID partidaId) {
        super(String.format("No se encontró una partida con ID '%s'", partidaId));
    }
}
