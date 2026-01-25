package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CalcularNominaCommand {
    private final ProyectoId proyectoId;
    private final LocalDate periodoInicio;
    private final LocalDate periodoFin;
    private final List<EmpleadoId> empleadoIds;

    public CalcularNominaCommand(UUID proyectoId, LocalDate periodoInicio, LocalDate periodoFin,
            List<UUID> empleadoIds) {
        this.proyectoId = ProyectoId.from(Objects.requireNonNull(proyectoId, "proyectoId cannot be null"));
        this.periodoInicio = Objects.requireNonNull(periodoInicio, "periodoInicio cannot be null");
        this.periodoFin = Objects.requireNonNull(periodoFin, "periodoFin cannot be null");
        this.empleadoIds = empleadoIds != null ? empleadoIds.stream().map(EmpleadoId::of).collect(Collectors.toList())
                : null; // If null, maybe calculate for all employees in project?
        // Requirement says "Calculate payroll for multiple employees".
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFin() {
        return periodoFin;
    }

    public List<EmpleadoId> getEmpleadoIds() {
        return empleadoIds;
    }
}
