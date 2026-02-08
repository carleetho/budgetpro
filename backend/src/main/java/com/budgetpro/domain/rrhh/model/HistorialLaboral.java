package com.budgetpro.domain.rrhh.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a period of employment history with specific conditions
 * (salary, position). Managed by the Empleado aggregate root.
 */
public class HistorialLaboral {

    private final HistorialId id;
    private final String cargo;
    private final BigDecimal salarioBase;
    private final TipoEmpleado tipoEmpleado;
    private final LocalDate fechaInicio;
    private LocalDate fechaFin; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.rrhh - End date set when employment record closed
    private String unidadSalario = "MENSUAL"; // Default

    private HistorialLaboral(HistorialId id, String cargo, BigDecimal salarioBase, TipoEmpleado tipoEmpleado,
            LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = Objects.requireNonNull(id, "HistorialId cannot be null");
        this.cargo = Objects.requireNonNull(cargo, "Position (cargo) cannot be null");
        this.salarioBase = Objects.requireNonNull(salarioBase, "Base salary cannot be null");
        this.tipoEmpleado = Objects.requireNonNull(tipoEmpleado, "Employment type cannot be null");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "Start date cannot be null");
        this.fechaFin = fechaFin;

        if (salarioBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
    }

    // Legacy constructor for backward compatibility if needed, or update existing
    // usage
    private HistorialLaboral(HistorialId id, String cargo, BigDecimal salarioBase, TipoEmpleado tipoEmpleado,
            LocalDate fechaInicio) {
        this(id, cargo, salarioBase, tipoEmpleado, fechaInicio, null);
    }

    public static HistorialLaboral crear(HistorialId id, String cargo, BigDecimal salarioBase,
            TipoEmpleado tipoEmpleado, LocalDate fechaInicio) {
        return new HistorialLaboral(id, cargo, salarioBase, tipoEmpleado, fechaInicio);
    }

    /**
     * Reconstitutes a history record from persistence.
     */
    public static HistorialLaboral reconstruir(HistorialId id, String cargo, BigDecimal salarioBase,
            TipoEmpleado tipoEmpleado, LocalDate fechaInicio, LocalDate fechaFin) {
        return new HistorialLaboral(id, cargo, salarioBase, tipoEmpleado, fechaInicio, fechaFin);
    }

    /**
     * Closes the current history record by setting the end date.
     * 
     * @param fechaCierre The date this record becomes invalid (usually the day
     *                    before the new record starts)
     */
    public void cerrar(LocalDate fechaCierre) {
        if (fechaCierre == null) {
            throw new IllegalArgumentException("Closing date cannot be null");
        }
        if (fechaCierre.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("Closing date cannot be before start date");
        }
        this.fechaFin = fechaCierre;
    }

    public boolean esActivo() {
        return fechaFin == null;
    }

    public boolean esValidoEnFecha(LocalDate fecha) {
        if (fecha == null)
            return false;
        boolean afterStart = !fecha.isBefore(fechaInicio);
        boolean beforeEnd = fechaFin == null || !fecha.isAfter(fechaFin);
        return afterStart && beforeEnd;
    }

    // Getters

    public HistorialId getId() {
        return id;
    }

    public String getCargo() {
        return cargo;
    }

    public BigDecimal getSalarioBase() {
        return salarioBase;
    }

    public TipoEmpleado getTipoEmpleado() {
        return tipoEmpleado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getUnidadSalario() {
        return unidadSalario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HistorialLaboral that = (HistorialLaboral) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
