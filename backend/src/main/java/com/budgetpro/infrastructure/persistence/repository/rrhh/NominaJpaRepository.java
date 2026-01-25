package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.infrastructure.persistence.entity.rrhh.NominaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface NominaJpaRepository extends JpaRepository<NominaEntity, UUID> {

    boolean existsByProyectoIdAndPeriodoInicioAndPeriodoFinAndEstadoIn(UUID proyectoId, LocalDate periodoInicio,
            LocalDate periodoFin, Collection<String> estados);

    List<NominaEntity> findByEstadoAndProyectoId(String estado, UUID proyectoId);
}
