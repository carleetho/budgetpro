package com.budgetpro.infrastructure.persistence.repository.alertas;

import com.budgetpro.infrastructure.persistence.entity.alertas.AnalisisPresupuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para AnalisisPresupuestoEntity.
 */
@Repository
public interface AnalisisPresupuestoJpaRepository extends JpaRepository<AnalisisPresupuestoEntity, UUID> {

    /**
     * Busca todos los análisis de un presupuesto ordenados por fecha descendente.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de análisis del presupuesto
     */
    List<AnalisisPresupuestoEntity> findByPresupuestoIdOrderByFechaAnalisisDesc(UUID presupuestoId);

    /**
     * Busca el último análisis de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return El último análisis del presupuesto
     */
    @Query("SELECT a FROM AnalisisPresupuestoEntity a WHERE a.presupuestoId = :presupuestoId ORDER BY a.fechaAnalisis DESC LIMIT 1")
    Optional<AnalisisPresupuestoEntity> findUltimoPorPresupuestoId(UUID presupuestoId);
}
