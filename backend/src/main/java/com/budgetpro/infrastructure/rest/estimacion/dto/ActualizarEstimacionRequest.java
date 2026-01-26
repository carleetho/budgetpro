package com.budgetpro.infrastructure.rest.estimacion.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ActualizarEstimacionRequest {

    @NotNull
    private LocalDate fechaInicio;
    @NotNull
    private LocalDate fechaFin;
    private BigDecimal retencionPorcentaje;

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getRetencionPorcentaje() {
        return retencionPorcentaje;
    }

    public void setRetencionPorcentaje(BigDecimal retencionPorcentaje) {
        this.retencionPorcentaje = retencionPorcentaje;
    }
}
