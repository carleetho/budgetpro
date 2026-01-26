package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para PartidaEntity.
 */
@Repository
public interface PartidaJpaRepository extends JpaRepository<PartidaEntity, UUID> {

    /**
     * Busca todas las partidas de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de partidas del presupuesto
     */
    List<PartidaEntity> findByPresupuestoId(UUID presupuestoId);

    /**
     * Busca todas las partidas raíz (sin padre) de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de partidas raíz
     */
    List<PartidaEntity> findByPresupuestoIdAndPadreIsNull(UUID presupuestoId);

    /**
     * Busca todas las partidas hijas de una partida padre.
     * 
     * @param padreId El ID de la partida padre
     * @return Lista de partidas hijas
     */
    List<PartidaEntity> findByPadreId(UUID padreId);
}
