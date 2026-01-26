package com.budgetpro.infrastructure.rest.estimacion.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CrearEstimacionRequest {

    @NotNull
    private UUID proyectoId;
    @NotNull
    private UUID presupuestoId;
    @NotNull
    private LocalDate fechaInicio;
    @NotNull
    private LocalDate fechaFin;
    private BigDecimal retencionPorcentaje; // Optional

    // Getters and Setters

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
