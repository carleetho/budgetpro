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
    Optional<ConfiguracionLaboralExtendidaEntity> findFirstByProyectoIsNullAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(
            LocalDate date);

    /**
     * Find project-specific configuration effective on or before the given date.
     */
    Optional<ConfiguracionLaboralExtendidaEntity> findFirstByProyecto_IdAndFechaVigenciaInicioLessThanEqualOrderByFechaVigenciaInicioDesc(
            UUID proyectoId, LocalDate date);

    Optional<ConfiguracionLaboralExtendidaEntity> findByProyecto_IdAndFechaVigenciaFinIsNull(UUID proyectoId);

    Optional<ConfiguracionLaboralExtendidaEntity> findByProyectoIsNullAndFechaVigenciaFinIsNull();

    List<ConfiguracionLaboralExtendidaEntity> findByProyecto_IdAndFechaVigenciaInicioBetweenOrderByFechaVigenciaInicioAsc(
            UUID proyectoId, LocalDate start, LocalDate end);

    List<ConfiguracionLaboralExtendidaEntity> findByProyectoIsNullAndFechaVigenciaInicioBetweenOrderByFechaVigenciaInicioAsc(
            LocalDate start, LocalDate end);
}
