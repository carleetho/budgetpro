package com.budgetpro.application.partida.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear una partida para un presupuesto que no existe.
 */
public class PresupuestoNoEncontradoException extends RuntimeException {

    public PresupuestoNoEncontradoException(UUID presupuestoId) {
        super(String.format("No se encontró un presupuesto con ID '%s'", presupuestoId));
    }
}
