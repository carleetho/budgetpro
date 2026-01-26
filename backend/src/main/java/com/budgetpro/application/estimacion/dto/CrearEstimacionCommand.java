package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CrearEstimacionCommand {

    private UUID proyectoId;
    private UUID presupuestoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal retencionPorcentaje;

    public CrearEstimacionCommand() {
    }

    public CrearEstimacionCommand(UUID proyectoId, UUID presupuestoId, LocalDate fechaInicio, LocalDate fechaFin,
            BigDecimal retencionPorcentaje) {
        this.proyectoId = proyectoId;
        this.presupuestoId = presupuestoId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.retencionPorcentaje = retencionPorcentaje;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(UUID proyectoId) {
        this.proyectoId = proyectoId;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(UUID presupuestoId) {
        this.presupuestoId = presupuestoId;
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
