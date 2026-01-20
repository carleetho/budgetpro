package com.budgetpro.application.apu.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un APU.
 */
public class ApuNoEncontradoException extends RuntimeException {

    public ApuNoEncontradoException(UUID apuId) {
        super("No se encontró el APU con ID: " + apuId);
    }
}
