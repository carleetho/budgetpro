package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para PresupuestoEntity.
 */
@Repository
public interface PresupuestoJpaRepository extends JpaRepository<PresupuestoEntity, UUID> {

    /**
     * Busca el presupuesto activo de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con el presupuesto si existe
     */
    Optional<PresupuestoEntity> findByProyectoId(UUID proyectoId);

    /**
     * Verifica si existe un presupuesto para el proyecto dado.
     * 
     * @param proyectoId El ID del proyecto
     * @return true si existe, false en caso contrario
     */
    boolean existsByProyectoId(UUID proyectoId);
}
