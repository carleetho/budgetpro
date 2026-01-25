package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.CalcularNominaCommand;
import com.budgetpro.application.rrhh.dto.NominaResponse;

public interface CalcularNominaUseCase {
    NominaResponse calcularNomina(CalcularNominaCommand command);
}
