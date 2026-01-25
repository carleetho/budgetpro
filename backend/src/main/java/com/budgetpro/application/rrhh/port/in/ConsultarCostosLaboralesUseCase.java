package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.ConsultarCostosLaboralesQuery;
import com.budgetpro.application.rrhh.dto.CostosLaboralesResponse;

public interface ConsultarCostosLaboralesUseCase {
    CostosLaboralesResponse consultarCostos(ConsultarCostosLaboralesQuery query);
}
