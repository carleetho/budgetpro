package com.budgetpro.infrastructure.persistence.repository.almacen;

import com.budgetpro.infrastructure.persistence.entity.almacen.AlmacenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para AlmacenEntity.
 */
@Repository
public interface AlmacenJpaRepository extends JpaRepository<AlmacenEntity, UUID> {

    /**
     * Busca todos los almacenes activos de un proyecto.
     */
    List<AlmacenEntity> findByProyectoIdAndActivoTrue(UUID proyectoId);
}
