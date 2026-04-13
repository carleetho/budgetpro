package com.budgetpro.application.recurso.port.in;

import com.budgetpro.application.recurso.dto.RecursoResponse;

import java.util.List;
import java.util.UUID;

public interface ObtenerRecursoUseCase {
    RecursoResponse obtenerPorId(UUID id);
    List<RecursoResponse> listar();
}

