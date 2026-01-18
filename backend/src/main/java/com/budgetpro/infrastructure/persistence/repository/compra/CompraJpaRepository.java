package com.budgetpro.infrastructure.persistence.repository.compra;

import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para CompraEntity.
 */
@Repository
public interface CompraJpaRepository extends JpaRepository<CompraEntity, UUID> {

    /**
     * Busca todas las compras de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de compras del proyecto
     */
    List<CompraEntity> findByProyectoId(UUID proyectoId);
}
