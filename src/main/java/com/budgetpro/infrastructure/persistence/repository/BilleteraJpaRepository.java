package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository para BilleteraEntity.
 * 
 * Proporciona métodos de acceso a datos para la entidad Billetera.
 */
@Repository
public interface BilleteraJpaRepository extends JpaRepository<BilleteraEntity, UUID> {

    /**
     * Busca una billetera por el ID del proyecto.
     * 
     * Cada proyecto tiene UNA sola billetera (relación 1:1).
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con la billetera si existe, vacío en caso contrario
     */
    Optional<BilleteraEntity> findByProyectoId(UUID proyectoId);

}
