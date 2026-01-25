package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.CrearEmpleadoCommand;
import com.budgetpro.application.rrhh.dto.EmpleadoResponse;

public interface CrearEmpleadoUseCase {
    EmpleadoResponse ejecutar(CrearEmpleadoCommand command);
}
