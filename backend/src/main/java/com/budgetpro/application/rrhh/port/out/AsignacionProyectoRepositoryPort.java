package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;

import java.time.LocalDate;

public interface AsignacionProyectoRepositoryPort {
    boolean existsActiveAssignment(EmpleadoId empleadoId);

    void save(AsignacionProyecto assignment);

    boolean existsOverlap(EmpleadoId employeeId, LocalDate start, LocalDate end);
}
