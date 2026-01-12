package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository para InventarioItemEntity.
 */
@Repository
public interface InventarioJpaRepository extends JpaRepository<InventarioItemEntity, UUID> {

    /**
     * Busca un inventario por proyecto y recurso.
     */
    Optional<InventarioItemEntity> findByProyectoIdAndRecursoId(UUID proyectoId, UUID recursoId);

    /**
     * Busca todos los inventarios de un proyecto.
     */
    List<InventarioItemEntity> findByProyectoId(UUID proyectoId);

    /**
     * Busca inventarios por lista de recursoIds (para un proyecto específico).
     */
    @Query("SELECT i FROM InventarioItemEntity i WHERE i.proyectoId = :proyectoId AND i.recurso.id IN :recursoIds")
    List<InventarioItemEntity> findByProyectoIdAndRecursoIds(@Param("proyectoId") UUID proyectoId, 
                                                              @Param("recursoIds") List<UUID> recursoIds);
}
