package com.budgetpro.application.estimacion.port.in;

import com.budgetpro.application.estimacion.dto.CrearEstimacionCommand;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;

public interface CrearEstimacionUseCase {
    EstimacionResponse crear(CrearEstimacionCommand command);
}
