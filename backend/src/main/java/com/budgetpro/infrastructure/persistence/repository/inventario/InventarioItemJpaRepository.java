package com.budgetpro.infrastructure.persistence.repository.inventario;

import com.budgetpro.infrastructure.persistence.entity.inventario.InventarioItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para InventarioItemEntity.
 */
@Repository
public interface InventarioItemJpaRepository extends JpaRepository<InventarioItemEntity, UUID> {

    /**
     * Busca un item de inventario por proyecto y recurso.
     * 
     * @param proyectoId El ID del proyecto
     * @param recursoId El ID del recurso
     * @return Optional con el item de inventario si existe
     */
    Optional<InventarioItemEntity> findByProyectoIdAndRecursoId(UUID proyectoId, UUID recursoId);

    /**
     * Busca todos los items de inventario de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de items de inventario del proyecto
     */
    List<InventarioItemEntity> findByProyectoId(UUID proyectoId);
}
