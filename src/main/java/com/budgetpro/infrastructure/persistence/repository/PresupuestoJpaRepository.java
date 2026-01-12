package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository para PresupuestoEntity.
 * 
 * Proporciona métodos de acceso a datos para la entidad Presupuesto.
 */
@Repository
public interface PresupuestoJpaRepository extends JpaRepository<PresupuestoEntity, UUID> {

    /**
     * Busca todos los presupuestos de un proyecto.
     */
    List<PresupuestoEntity> findByProyectoId(UUID proyectoId);

    /**
     * Busca un presupuesto por su ID.
     */
    Optional<PresupuestoEntity> findById(UUID id);

    /**
     * Verifica si existe un presupuesto con el ID especificado.
     */
    boolean existsById(UUID id);
}
