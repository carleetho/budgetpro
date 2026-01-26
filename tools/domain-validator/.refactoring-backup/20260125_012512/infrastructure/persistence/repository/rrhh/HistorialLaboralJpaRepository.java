package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.infrastructure.persistence.entity.rrhh.HistorialLaboralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistorialLaboralJpaRepository extends JpaRepository<HistorialLaboralEntity, UUID> {

    Optional<HistorialLaboralEntity> findByEmpleadoIdAndFechaFinIsNull(UUID empleadoId);

    List<HistorialLaboralEntity> findByEmpleadoIdOrderByFechaInicioDesc(UUID empleadoId);

    @Query("SELECT h FROM HistorialLaboralEntity h " + "WHERE h.empleado.id = :empleadoId "
            + "AND h.fechaInicio <= :fecha " + "AND (h.fechaFin IS NULL OR h.fechaFin >= :fecha)")
    Optional<HistorialLaboralEntity> findByEmpleadoIdAndFecha(@Param("empleadoId") UUID empleadoId,
            @Param("fecha") LocalDate fecha);
}
