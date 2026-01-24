package com.budgetpro.infrastructure.persistence.repository.requisicion;

import com.budgetpro.infrastructure.persistence.entity.requisicion.RequisicionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para RequisicionEntity.
 */
@Repository
public interface RequisicionJpaRepository extends JpaRepository<RequisicionEntity, UUID> {

    /**
     * Busca todas las requisiciones de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de requisiciones del proyecto
     */
    List<RequisicionEntity> findByProyectoId(UUID proyectoId);
}
