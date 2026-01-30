package com.budgetpro.domain.finanzas.estimacion.model;

public class Estimacion {
    private EstadoEstimacion estado;

    public void directAssignment() {
        // ruleid: 01-estimacion-state-machine
        this.estado = EstadoEstimacion.APROBADA;
    }

    public void anotherDirectAssignment(Estimacion other) {
        // ruleid: 01-estimacion-state-machine
        other.estado = EstadoEstimacion.FACTURADA;
    }

    public void aprobar() {
        // ok: 01-estimacion-state-machine
        this.estado = EstadoEstimacion.APROBADA;
    }

    public void facturar() {
        // ok: 01-estimacion-state-machine
        this.estado = EstadoEstimacion.FACTURADA;
    }

    public void anular() {
        // ok: 01-estimacion-state-machine
        this.estado = EstadoEstimacion.ANULADA;
    }
}
