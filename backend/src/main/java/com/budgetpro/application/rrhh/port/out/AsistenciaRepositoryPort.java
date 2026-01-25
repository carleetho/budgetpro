package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AsistenciaRepositoryPort {
    AsistenciaRegistro save(AsistenciaRegistro asistencia);

    List<AsistenciaRegistro> findByEmpleadoAndPeriodo(EmpleadoId empleadoId, LocalDate startDate, LocalDate endDate);

    List<AsistenciaRegistro> findByEmpleadosAndPeriodo(List<EmpleadoId> empleadoIds, LocalDate startDate,
            LocalDate endDate);

    List<AsistenciaRegistro> findOverlapping(EmpleadoId empleadoId, LocalDateTime start, LocalDateTime end);

    List<AsistenciaRegistro> findByProyectoAndPeriodo(ProyectoId proyectoId, LocalDate startDate, LocalDate endDate);
}
