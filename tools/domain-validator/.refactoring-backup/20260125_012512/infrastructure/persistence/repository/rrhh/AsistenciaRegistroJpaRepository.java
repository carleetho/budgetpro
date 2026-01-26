package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.infrastructure.persistence.entity.rrhh.AsistenciaRegistroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AsistenciaRegistroJpaRepository extends JpaRepository<AsistenciaRegistroEntity, UUID> {

    List<AsistenciaRegistroEntity> findByEmpleadoIdAndFechaBetween(UUID empleadoId, LocalDate inicio, LocalDate fin);

    List<AsistenciaRegistroEntity> findByEmpleadoIdInAndFechaBetween(List<UUID> empleadoIds, LocalDate inicio,
            LocalDate fin);

    List<AsistenciaRegistroEntity> findByProyectoIdAndFechaBetween(UUID proyectoId, LocalDate inicio, LocalDate fin);

    boolean existsByEmpleadoIdAndFechaAndHoraEntradaBetween(UUID empleadoId, LocalDate fecha, LocalTime start,
            LocalTime end);
}
