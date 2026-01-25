package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.ConfiguracionLaboralExtendidaResponse;
import com.budgetpro.application.rrhh.dto.ConfigurarLaboralExtendidaCommand;

public interface ConfigurarLaboralExtendidaUseCase {
    ConfiguracionLaboralExtendidaResponse configurar(ConfigurarLaboralExtendidaCommand command);
}
