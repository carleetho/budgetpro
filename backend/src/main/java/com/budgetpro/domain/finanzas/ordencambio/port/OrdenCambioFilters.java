package com.budgetpro.domain.finanzas.ordencambio.port;

import com.budgetpro.domain.finanzas.ordencambio.model.EstadoOrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrigenOrdenCambio;

import java.time.LocalDate;

/**
 * DTO para encapsular los criterios de filtrado de órdenes de cambio.
 */
public class OrdenCambioFilters {
    private final EstadoOrdenCambio estado;
    private final OrigenOrdenCambio origen;
    private final LocalDate fechaDesde;
    private final LocalDate fechaHasta;

    public OrdenCambioFilters(EstadoOrdenCambio estado, OrigenOrdenCambio origen, LocalDate fechaDesde,
            LocalDate fechaHasta) {
        this.estado = estado;
        this.origen = origen;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public EstadoOrdenCambio getEstado() {
        return estado;
    }

    public OrigenOrdenCambio getOrigen() {
        return origen;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }
}
