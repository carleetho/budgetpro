package com.budgetpro.domain.rrhh.model;

import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity representing an employee's assignment to a project. Links the
 * employee with a RecursoProxy for cost tracking.
 */
public class AsignacionProyecto {

    private final AsignacionProyectoId id;
    private final EmpleadoId empleadoId;
    private final ProyectoId proyectoId;
    private final RecursoProxyId recursoProxyId;
    private final LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal tarifaHora;
    private String rolProyecto;

    private AsignacionProyecto(AsignacionProyectoId id, EmpleadoId empleadoId, ProyectoId proyectoId,
            RecursoProxyId recursoProxyId, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal tarifaHora,
            String rolProyecto) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.empleadoId = Objects.requireNonNull(empleadoId, "EmpleadoId cannot be null");
        this.proyectoId = Objects.requireNonNull(proyectoId, "ProyectoId cannot be null");
        this.recursoProxyId = Objects.requireNonNull(recursoProxyId, "RecursoProxyId cannot be null");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "Start date cannot be null");
        this.fechaFin = fechaFin;
        this.tarifaHora = tarifaHora;
        this.rolProyecto = rolProyecto;

        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("Fecha fin cannot be before fecha inicio");
        }
        if (tarifaHora != null && tarifaHora.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tarifa hora cannot be negative");
        }
    }

    public static AsignacionProyecto crear(AsignacionProyectoId id, EmpleadoId empleadoId, ProyectoId proyectoId,
            RecursoProxyId recursoProxyId, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal tarifaHora,
            String rolProyecto) {
        return new AsignacionProyecto(id, empleadoId, proyectoId, recursoProxyId, fechaInicio, fechaFin, tarifaHora,
                rolProyecto);
    }

    public static AsignacionProyecto reconstruir(AsignacionProyectoId id, EmpleadoId empleadoId, ProyectoId proyectoId,
            RecursoProxyId recursoProxyId, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal tarifaHora,
            String rolProyecto) {
        return new AsignacionProyecto(id, empleadoId, proyectoId, recursoProxyId, fechaInicio, fechaFin, tarifaHora,
                rolProyecto);
    }

    public void finalizar(LocalDate fechaFin) {
        if (fechaFin == null) {
            throw new IllegalArgumentException("Fecha fin cannot be null when finalizing");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("Fecha fin cannot be before fecha inicio");
        }
        this.fechaFin = fechaFin;
    }

    // Getters
    public AsignacionProyectoId getId() {
        return id;
    }

    public EmpleadoId getEmpleadoId() {
        return empleadoId;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public RecursoProxyId getRecursoProxyId() {
        return recursoProxyId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public BigDecimal getTarifaHora() {
        return tarifaHora;
    }

    public String getRolProyecto() {
        return rolProyecto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AsignacionProyecto that = (AsignacionProyecto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
