package com.budgetpro.application.apu.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear un APU para una partida que ya tiene uno.
 */
public class ApuYaExisteException extends RuntimeException {

    public ApuYaExisteException(UUID partidaId) {
        super(String.format("La partida con ID '%s' ya tiene un APU asociado", partidaId));
    }
}
