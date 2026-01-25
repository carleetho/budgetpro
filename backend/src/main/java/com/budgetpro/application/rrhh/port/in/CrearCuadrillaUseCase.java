package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.CrearCuadrillaCommand;
import com.budgetpro.application.rrhh.dto.CuadrillaResponse;

public interface CrearCuadrillaUseCase {
    CuadrillaResponse crearCuadrilla(CrearCuadrillaCommand command);
}
