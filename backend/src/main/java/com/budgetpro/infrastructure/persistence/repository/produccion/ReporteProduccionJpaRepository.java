package com.budgetpro.infrastructure.persistence.repository.produccion;

import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ReporteProduccionEntity.
 */
public interface ReporteProduccionJpaRepository extends JpaRepository<ReporteProduccionEntity, UUID> {

    @EntityGraph(attributePaths = "detalles")
    Optional<ReporteProduccionEntity> findWithDetallesById(UUID id);

    @Query("""
        SELECT DISTINCT r
        FROM ReporteProduccionEntity r
        JOIN r.detalles d
        JOIN d.partida p
        JOIN p.presupuesto pre
        WHERE pre.proyectoId = :proyectoId
        ORDER BY r.fechaReporte DESC
        """)
    List<ReporteProduccionEntity> findByProyectoId(@Param("proyectoId") UUID proyectoId);
}
