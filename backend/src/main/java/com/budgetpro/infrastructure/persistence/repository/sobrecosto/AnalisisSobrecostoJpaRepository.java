package com.budgetpro.infrastructure.persistence.repository.sobrecosto;

import com.budgetpro.infrastructure.persistence.entity.sobrecosto.AnalisisSobrecostoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para AnalisisSobrecostoEntity.
 */
@Repository
public interface AnalisisSobrecostoJpaRepository extends JpaRepository<AnalisisSobrecostoEntity, UUID> {

    /**
     * Busca el análisis de sobrecosto de un presupuesto (relación 1:1).
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Optional con el análisis si existe
     */
    Optional<AnalisisSobrecostoEntity> findByPresupuestoId(UUID presupuestoId);
}
