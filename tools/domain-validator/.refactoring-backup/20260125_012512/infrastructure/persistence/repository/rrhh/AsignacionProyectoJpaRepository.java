package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.infrastructure.persistence.entity.rrhh.AsignacionProyectoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AsignacionProyectoJpaRepository extends JpaRepository<AsignacionProyectoEntity, UUID> {
    boolean existsByEmpleado_IdAndFechaFinIsNull(UUID empleadoId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) > 0 FROM AsignacionProyectoEntity a "
            + "WHERE a.empleado.id = :empleadoId " + "AND (a.fechaFin IS NULL OR a.fechaFin >= :startDate) "
            + "AND a.fechaInicio <= :endDate")
    boolean existsOverlap(@org.springframework.data.repository.query.Param("empleadoId") UUID empleadoId,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}
