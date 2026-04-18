package com.budgetpro.application.presupuesto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta acceder a un presupuesto que no existe.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PresupuestoNoEncontradoException extends RuntimeException {

    public PresupuestoNoEncontradoException(UUID presupuestoId) {
        super(String.format("No se encontró un presupuesto con ID '%s'", presupuestoId));
    }
}
