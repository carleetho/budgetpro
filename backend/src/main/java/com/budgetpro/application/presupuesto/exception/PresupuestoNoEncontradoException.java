package com.budgetpro.application.presupuesto.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta acceder a un presupuesto que no existe.
 */
public class PresupuestoNoEncontradoException extends RuntimeException {

    public PresupuestoNoEncontradoException(UUID presupuestoId) {
        super(String.format("No se encontró un presupuesto con ID '%s'", presupuestoId));
    }
}
