package com.budgetpro.application.presupuesto.port.in;

import com.budgetpro.application.presupuesto.dto.ListarPresupuestosPaginadosResponse;

import java.util.UUID;

public interface ListarPresupuestosPaginadosUseCase {

    ListarPresupuestosPaginadosResponse listar(UUID tenantId, UUID proyectoId, int page, int size);
}
