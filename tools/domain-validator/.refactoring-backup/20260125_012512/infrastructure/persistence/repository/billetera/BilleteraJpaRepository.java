package com.budgetpro.infrastructure.persistence.repository.billetera;

import com.budgetpro.infrastructure.persistence.entity.billetera.BilleteraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para BilleteraEntity.
 */
@Repository
public interface BilleteraJpaRepository extends JpaRepository<BilleteraEntity, UUID> {

    /**
     * Busca una billetera por el ID del proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con la billetera si existe
     */
    Optional<BilleteraEntity> findByProyectoId(UUID proyectoId);
}
