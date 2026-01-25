package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.ActualizarEmpleadoCommand;
import com.budgetpro.application.rrhh.dto.EmpleadoResponse;

public interface ActualizarEmpleadoUseCase {
    EmpleadoResponse ejecutar(ActualizarEmpleadoCommand command);
}
