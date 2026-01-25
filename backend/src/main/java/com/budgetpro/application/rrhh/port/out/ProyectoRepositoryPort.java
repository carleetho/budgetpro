package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.proyecto.model.ProyectoId;

public interface ProyectoRepositoryPort {
    boolean existsById(ProyectoId id);
}
