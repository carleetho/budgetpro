package com.budgetpro.application.partida.port.in;

import com.budgetpro.application.partida.dto.WbsNodeResponse;

import java.util.List;
import java.util.UUID;

public interface ObtenerWbsUseCase {
    List<WbsNodeResponse> obtenerWbsPorPresupuesto(UUID presupuestoId);
}

