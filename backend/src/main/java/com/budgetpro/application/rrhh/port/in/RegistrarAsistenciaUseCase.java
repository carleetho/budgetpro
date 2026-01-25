package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.RegistrarAsistenciaCommand;
import com.budgetpro.application.rrhh.dto.AsistenciaResponse;

public interface RegistrarAsistenciaUseCase {
    AsistenciaResponse registrarAsistencia(RegistrarAsistenciaCommand command);
}
