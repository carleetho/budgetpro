package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.rrhh.model.ConfiguracionLaboralExtendida;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConfiguracionLaboralRepositoryPort {
    // Deprecated or Legacy method
    Optional<com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral> findEffectiveConfig(
            ProyectoId proyectoId, LocalDate fecha);

    // New Extended Methods
    ConfiguracionLaboralExtendida save(ConfiguracionLaboralExtendida config);

    Optional<ConfiguracionLaboralExtendida> findActiveByProyecto(ProyectoId proyectoId);

    Optional<ConfiguracionLaboralExtendida> findGlobalActive();

    List<ConfiguracionLaboralExtendida> findHistoryByProyecto(ProyectoId proyectoId, LocalDate start, LocalDate end);

    List<ConfiguracionLaboralExtendida> findHistoryGlobal(LocalDate start, LocalDate end);
}
