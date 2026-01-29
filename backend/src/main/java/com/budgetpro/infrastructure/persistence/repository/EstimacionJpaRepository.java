package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EstimacionJpaRepository extends JpaRepository<EstimacionEntity, UUID> {

        List<EstimacionEntity> findByPresupuestoId(UUID presupuestoId); // Implicitly filtered by project via service
                                                                        // layer
                                                                        // logic usually, or explicitly if needed.
        // The requirement says "findByProyectoId", but Estimacion links to Presupuesto.
        // Usually Presupuesto links to Proyecto.
        // If we need to find by Proyecto directly, we need a join or store proyectoId
        // in Estimacion.
        // The migration V19 doesn't include proyecto_id, only presupuesto_id.
        // Let's assume we filter by PresupuestoId which maps to a Project.
        // Wait, the EstimacionRepository port has findByProyectoId.
        // And Estimacion aggregates root usually has simple ID refs.
        // If I look at the Estimacion aggregate, does it have proyectoId?
        // Let's check Estimacion.java content later if needed.
        // But V19 sql didn't have proyecto_id.
        // If the port requires findByProyectoId, I likely need to join with Presupuesto
        // table.

        @Query("SELECT e FROM EstimacionEntity e WHERE e.presupuestoId IN (SELECT p.id FROM PresupuestoEntity p WHERE p.proyectoId = :proyectoId)")
        List<EstimacionEntity> findByProyectoId(@Param("proyectoId") UUID proyectoId);

        @Query("SELECT e FROM EstimacionEntity e WHERE e.presupuestoId IN (SELECT p.id FROM PresupuestoEntity p WHERE p.proyectoId = :proyectoId) AND e.estado = :estado")
        List<EstimacionEntity> findByProyectoIdAndEstado(@Param("proyectoId") UUID proyectoId,
                        @Param("estado") EstadoEstimacion estado);

        @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EstimacionEntity e "
                        + "WHERE e.presupuestoId IN (SELECT p.id FROM PresupuestoEntity p WHERE p.proyectoId = :proyectoId) "
                        + "AND e.estado <> 'ANULADA' " + "AND ((e.fechaInicio <= :fin AND e.fechaFin >= :inicio))")
        boolean existsPeriodoSolapado(@Param("proyectoId") UUID proyectoId, @Param("inicio") LocalDate inicio,
                        @Param("fin") LocalDate fin);

        @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EstimacionEntity e "
                        + "WHERE e.presupuestoId IN (SELECT p.id FROM PresupuestoEntity p WHERE p.proyectoId = :proyectoId) "
                        + "AND e.estado <> 'ANULADA' " + "AND e.id <> :excludeId "
                        + "AND ((e.fechaInicio <= :fin AND e.fechaFin >= :inicio))")
        boolean existsPeriodoSolapadoExcludingId(@Param("proyectoId") UUID proyectoId,
                        @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin,
                        @Param("excludeId") UUID excludeId);

        @Query("SELECT MAX(e.numeroEstimacion) FROM EstimacionEntity e WHERE e.presupuestoId IN (SELECT p.id FROM PresupuestoEntity p WHERE p.proyectoId = :proyectoId)")
        Integer findMaxNumeroEstimacionByProyectoId(@Param("proyectoId") UUID proyectoId);
}
