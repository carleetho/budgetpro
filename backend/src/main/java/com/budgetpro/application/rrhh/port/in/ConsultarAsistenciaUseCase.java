package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.dto.ResumenAsistenciaResponse;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.time.LocalDate;
import java.util.List;

public interface ConsultarAsistenciaUseCase {
    List<AsistenciaResponse> consultarPorEmpleado(EmpleadoId empleadoId, LocalDate fechaInicio, LocalDate fechaFin);

    List<AsistenciaResponse> consultarPorProyecto(ProyectoId proyectoId, LocalDate fechaInicio, LocalDate fechaFin);

    ResumenAsistenciaResponse generarResumenMensual(EmpleadoId empleadoId, int mes, int ano);
}
