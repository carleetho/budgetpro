package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.rrhh.model.Nomina;
import com.budgetpro.domain.rrhh.model.NominaId;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.time.LocalDate;
import java.util.Optional;

public interface NominaRepositoryPort {
    Nomina save(Nomina nomina);

    Optional<Nomina> findById(NominaId id);

    boolean existsForPeriod(ProyectoId proyectoId, LocalDate periodoInicio, LocalDate periodoFin);
}
