package com.budgetpro.application.estimacion.port.in;

import com.budgetpro.application.estimacion.dto.ActualizarEstimacionCommand;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import java.util.UUID;

public interface ActualizarEstimacionUseCase {
    EstimacionResponse actualizar(UUID estimacionId, ActualizarEstimacionCommand command);
}
