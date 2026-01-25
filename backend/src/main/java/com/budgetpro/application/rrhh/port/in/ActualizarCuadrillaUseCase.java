package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.ActualizarCuadrillaCommand;
import com.budgetpro.application.rrhh.dto.CuadrillaResponse;

public interface ActualizarCuadrillaUseCase {
    CuadrillaResponse actualizarCuadrilla(ActualizarCuadrillaCommand command);
}
