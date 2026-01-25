package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.AsignacionProyectoResponse;
import com.budgetpro.application.rrhh.dto.AsignarEmpleadoProyectoCommand;

public interface AsignarEmpleadoProyectoUseCase {
    AsignacionProyectoResponse asignar(AsignarEmpleadoProyectoCommand command);
}
