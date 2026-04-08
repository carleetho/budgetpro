package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.util.Optional;

public interface ProyectoRepositoryPort {

    boolean existsById(ProyectoId id);

    /**
     * Carga el proyecto para validaciones de negocio (p. ej. REGLA-150 estado ACTIVO).
     */
    Optional<Proyecto> findById(ProyectoId id);
}
