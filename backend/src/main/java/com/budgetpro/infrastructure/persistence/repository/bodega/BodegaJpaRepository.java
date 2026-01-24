package com.budgetpro.infrastructure.persistence.repository.bodega;

import com.budgetpro.infrastructure.persistence.entity.BodegaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para BodegaEntity.
 */
@Repository
public interface BodegaJpaRepository extends JpaRepository<BodegaEntity, UUID> {

    /**
     * Busca bodegas activas por proyecto, ordenadas por código.
     *
     * @param proyectoId ID del proyecto
     * @return Lista de bodegas del proyecto
     */
    List<BodegaEntity> findByProyectoIdAndActivaTrueOrderByCodigoAsc(UUID proyectoId);
}
