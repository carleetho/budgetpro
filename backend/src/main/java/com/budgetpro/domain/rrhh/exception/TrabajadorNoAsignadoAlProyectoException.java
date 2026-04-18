package com.budgetpro.domain.rrhh.exception;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.EmpleadoId;

import java.time.LocalDate;
import java.util.Objects;

/**
 * REGLA-125: el trabajador debe tener asignación vigente al proyecto en la fecha del tareo.
 */
public final class TrabajadorNoAsignadoAlProyectoException extends RuntimeException {

    private final EmpleadoId empleadoId;
    private final ProyectoId proyectoId;
    private final LocalDate fecha;

    public TrabajadorNoAsignadoAlProyectoException(EmpleadoId empleadoId, ProyectoId proyectoId, LocalDate fecha,
            String message) {
        super(message);
        this.empleadoId = Objects.requireNonNull(empleadoId, "empleadoId");
        this.proyectoId = Objects.requireNonNull(proyectoId, "proyectoId");
        this.fecha = Objects.requireNonNull(fecha, "fecha");
    }

    public EmpleadoId getEmpleadoId() {
        return empleadoId;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
