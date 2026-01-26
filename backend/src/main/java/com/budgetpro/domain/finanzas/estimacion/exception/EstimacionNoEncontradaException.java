package com.budgetpro.domain.finanzas.estimacion.exception;

import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;

public class EstimacionNoEncontradaException extends RuntimeException {

    public EstimacionNoEncontradaException(EstimacionId id) {
        super("No se encontró la estimación con ID: " + id.getValue());
    }
}
