package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ActualizarEstimacionCommand {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal retencionPorcentaje;

    public ActualizarEstimacionCommand() {
    }

    public ActualizarEstimacionCommand(LocalDate fechaInicio, LocalDate fechaFin, BigDecimal retencionPorcentaje) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.retencionPorcentaje = retencionPorcentaje;
    }

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
