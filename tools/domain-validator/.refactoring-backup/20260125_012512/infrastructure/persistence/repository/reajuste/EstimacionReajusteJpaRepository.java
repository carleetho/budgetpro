package com.budgetpro.infrastructure.persistence.repository.reajuste;

import com.budgetpro.infrastructure.persistence.entity.reajuste.EstimacionReajusteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para EstimacionReajusteEntity.
 */
@Repository
public interface EstimacionReajusteJpaRepository extends JpaRepository<EstimacionReajusteEntity, UUID> {

    /**
     * Busca todas las estimaciones de un proyecto ordenadas por número descendente.
     */
    List<EstimacionReajusteEntity> findByProyectoIdOrderByNumeroEstimacionDesc(UUID proyectoId);

    /**
     * Busca todas las estimaciones de un presupuesto ordenadas por número descendente.
     */
    List<EstimacionReajusteEntity> findByPresupuestoIdOrderByNumeroEstimacionDesc(UUID presupuestoId);

    /**
     * Obtiene el siguiente número de estimación para un proyecto.
     */
    @Query("SELECT COALESCE(MAX(e.numeroEstimacion), 0) + 1 FROM EstimacionReajusteEntity e WHERE e.proyectoId = :proyectoId")
    Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId);
}
