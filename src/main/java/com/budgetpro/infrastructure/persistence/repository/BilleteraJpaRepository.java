package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad BilleteraEntity.
 * Proporciona operaciones de persistencia básicas.
 */
@Repository
public interface BilleteraJpaRepository extends JpaRepository<BilleteraEntity, UUID> {

    /**
     * Busca una billetera por el ID del proyecto.
     * 
     * Cada proyecto tiene UNA sola billetera (relación 1:1 con UNIQUE constraint).
     * 
     * @param proyectoId El ID del proyecto
     * @return Un Optional con la billetera si existe
     */
    Optional<BilleteraEntity> findByProyectoId(UUID proyectoId);

    /**
     * Verifica si existe una billetera para el proyecto dado.
     * 
     * @param proyectoId El ID del proyecto
     * @return true si existe, false en caso contrario
     */
    boolean existsByProyectoId(UUID proyectoId);
}
