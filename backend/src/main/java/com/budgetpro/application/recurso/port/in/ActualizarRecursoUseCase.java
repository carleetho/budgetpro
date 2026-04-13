package com.budgetpro.application.recurso.port.in;

import com.budgetpro.application.recurso.dto.ActualizarRecursoCommand;
import com.budgetpro.application.recurso.dto.RecursoResponse;

public interface ActualizarRecursoUseCase {
    RecursoResponse actualizar(ActualizarRecursoCommand command);
}

