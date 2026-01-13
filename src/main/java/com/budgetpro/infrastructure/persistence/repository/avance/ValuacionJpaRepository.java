package com.budgetpro.infrastructure.persistence.repository.avance;

import com.budgetpro.infrastructure.persistence.entity.avance.ValuacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ValuacionEntity.
 */
@Repository
public interface ValuacionJpaRepository extends JpaRepository<ValuacionEntity, UUID> {

    /**
     * Busca todas las valuaciones de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de valuaciones del proyecto
     */
    List<ValuacionEntity> findByProyectoId(UUID proyectoId);
}
