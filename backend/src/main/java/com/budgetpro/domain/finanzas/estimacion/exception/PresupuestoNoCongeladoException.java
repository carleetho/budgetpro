package com.budgetpro.domain.finanzas.estimacion.exception;

import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

public class PresupuestoNoCongeladoException extends RuntimeException {

    public PresupuestoNoCongeladoException(PresupuestoId id) {
        super("El presupuesto " + id.getValue() + " no está congelado/aprobado. No se pueden crear estimaciones.");
    }
}
