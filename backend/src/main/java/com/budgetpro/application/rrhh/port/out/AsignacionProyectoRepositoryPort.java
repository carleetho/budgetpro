package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.time.LocalDate;
import java.util.List;

public interface AsignacionProyectoRepositoryPort {
    boolean existsActiveAssignment(EmpleadoId empleadoId);

    void save(AsignacionProyecto assignment);

    boolean existsOverlap(EmpleadoId employeeId, LocalDate start, LocalDate end);

    /**
     * REGLA-125: asignación vigente del empleado al proyecto en la fecha indicada (inclusive en ambos extremos).
     */
    boolean existsVigenteAsignacionEmpleadoProyectoEnFecha(EmpleadoId empleadoId, ProyectoId proyectoId, LocalDate fecha);

    /**
     * Todas las asignaciones persistidas del empleado (para validación R-03 en dominio).
     */
    List<AsignacionProyecto> findAsignacionesByEmpleadoId(EmpleadoId empleadoId);
}
