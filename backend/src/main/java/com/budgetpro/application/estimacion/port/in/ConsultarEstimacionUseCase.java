package com.budgetpro.application.estimacion.port.in;

import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import java.util.UUID;

public interface ConsultarEstimacionUseCase {
    EstimacionResponse consultar(UUID estimacionId);
}
