package com.budgetpro.application.estimacion.port.in;

import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import java.util.List;
import java.util.UUID;

public interface ListarEstimacionesPorProyectoUseCase {
    List<EstimacionResponse> listar(UUID proyectoId);
}
