package com.budgetpro.application.estimacion.port.in;

import com.budgetpro.application.estimacion.dto.ActualizarItemsCommand;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import java.util.UUID;

public interface ActualizarItemsEstimacionUseCase {
    EstimacionResponse actualizarItems(UUID estimacionId, ActualizarItemsCommand command);
}
