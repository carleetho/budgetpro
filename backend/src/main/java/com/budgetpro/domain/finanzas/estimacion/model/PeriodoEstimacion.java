package com.budgetpro.domain.finanzas.estimacion.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object que representa el periodo de una estimación. Invariante:
 * fechaInicio <= fechaFin.
 */
public final class PeriodoEstimacion {

    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    private PeriodoEstimacion(LocalDate fechaInicio, LocalDate fechaFin) {
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "La fecha de inicio no puede ser nula");
        this.fechaFin = Objects.requireNonNull(fechaFin, "La fecha de fin no puede ser nula");

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    public static PeriodoEstimacion of(LocalDate fechaInicio, LocalDate fechaFin) {
        return new PeriodoEstimacion(fechaInicio, fechaFin);
    }

    /**
     * Reconstruye un periodo desde persistencia u otra capa. Mismo comportamiento
     * que of() pero semánticamente distinto para Clean Arch.
     */
    public static PeriodoEstimacion reconstruir(LocalDate fechaInicio, LocalDate fechaFin) {
        return new PeriodoEstimacion(fechaInicio, fechaFin);
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public boolean overlaps(PeriodoEstimacion other) {
        return !this.fechaFin.isBefore(other.fechaInicio) && !this.fechaInicio.isAfter(other.fechaFin);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PeriodoEstimacion that = (PeriodoEstimacion) o;
        return Objects.equals(fechaInicio, that.fechaInicio) && Objects.equals(fechaFin, that.fechaFin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fechaInicio, fechaFin);
    }

    @Override
    public String toString() {
        return fechaInicio + " - " + fechaFin;
    }
}
