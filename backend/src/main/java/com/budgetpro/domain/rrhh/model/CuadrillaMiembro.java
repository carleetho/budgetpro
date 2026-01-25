package com.budgetpro.domain.rrhh.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a member within a Crew (Cuadrilla). Contains information
 * about the employee's role and their period of membership.
 */
public class CuadrillaMiembro {

    private final CuadrillaMiembroId id;
    private final EmpleadoId empleadoId;
    private final String rol;
    private final LocalDate fechaIngreso;
    private LocalDate fechaSalida;

    private CuadrillaMiembro(CuadrillaMiembroId id, EmpleadoId empleadoId, String rol, LocalDate fechaIngreso,
            LocalDate fechaSalida) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.empleadoId = Objects.requireNonNull(empleadoId, "EmpleadoId cannot be null");
        this.rol = Objects.requireNonNull(rol, "Rol cannot be null");
        this.fechaIngreso = Objects.requireNonNull(fechaIngreso, "FechaIngreso cannot be null");
        this.fechaSalida = fechaSalida; // Can be null (active member)

        if (fechaSalida != null && fechaSalida.isBefore(fechaIngreso)) {
            throw new IllegalArgumentException("FechaSalida cannot be before FechaIngreso");
        }
    }

    public static CuadrillaMiembro crear(CuadrillaMiembroId id, EmpleadoId empleadoId, String rol,
            LocalDate fechaIngreso) {
        return new CuadrillaMiembro(id, empleadoId, rol, fechaIngreso, null);
    }

    // Internal factory for reconstruction if needed, or strictly use constructor

    public void remover(LocalDate fechaSalida) {
        if (fechaSalida == null) {
            throw new IllegalArgumentException("FechaSalida cannot be null");
        }
        if (fechaSalida.isBefore(this.fechaIngreso)) {
            throw new IllegalArgumentException("FechaSalida cannot be before FechaIngreso");
        }
        this.fechaSalida = fechaSalida;
    }

    public boolean esActivo() {
        return fechaSalida == null;
    }

    public CuadrillaMiembroId getId() {
        return id;
    }

    public EmpleadoId getEmpleadoId() {
        return empleadoId;
    }

    public String getRol() {
        return rol;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CuadrillaMiembro that = (CuadrillaMiembro) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
