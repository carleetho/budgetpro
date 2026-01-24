package com.budgetpro.domain.logistica.requisicion.exception;

import com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;

/**
 * Excepción de dominio lanzada cuando se intenta despachar una requisición
 * que no está en estado APROBADA, DESPACHADA_PARCIAL o PENDIENTE_COMPRA.
 */
public class RequisicionNoAprobadaException extends RuntimeException {

    private final RequisicionId requisicionId;
    private final EstadoRequisicion estadoActual;

    public RequisicionNoAprobadaException(RequisicionId requisicionId, EstadoRequisicion estadoActual) {
        super(String.format(
            "No se puede despachar la requisición %s. Estado actual: %s. " +
            "Debe estar en APROBADA, DESPACHADA_PARCIAL o PENDIENTE_COMPRA.",
            requisicionId, estadoActual
        ));
        this.requisicionId = requisicionId;
        this.estadoActual = estadoActual;
    }

    public RequisicionId getRequisicionId() {
        return requisicionId;
    }

    public EstadoRequisicion getEstadoActual() {
        return estadoActual;
    }
}
