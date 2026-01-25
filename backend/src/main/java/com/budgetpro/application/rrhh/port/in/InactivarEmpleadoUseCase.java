package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.EmpleadoResponse;

public interface InactivarEmpleadoUseCase {
    EmpleadoResponse ejecutar(String id);
}
