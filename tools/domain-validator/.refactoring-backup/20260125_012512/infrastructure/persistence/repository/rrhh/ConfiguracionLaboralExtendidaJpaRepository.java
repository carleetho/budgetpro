package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.infrastructure.persistence.entity.rrhh.ConfiguracionLaboralExtendidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionLaboralExtendidaJpaRepository
        extends JpaRepository<ConfiguracionLaboralExtendidaEntity, UUID> {

    /**
     * Find global configuration effective on or before the given date.
     */
    Optional<ConfiguracionLaboralExtendidaEntity> findFirstByProyectoIdIsNullAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(
            LocalDate date);

    /**
     * Find project-specific configuration effective on or before the given date.
     */
    Optional<ConfiguracionLaboralExtendidaEntity> findFirstByProyectoIdAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(
            UUID proyectoId, LocalDate date);
}
