package com.budgetpro.infrastructure.persistence.repository.compra;

import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository para CompraEntity.
 * 
 * Proporciona métodos de acceso a datos para la entidad Compra,
 * incluyendo operaciones básicas de persistencia.
 */
@Repository
public interface CompraJpaRepository extends JpaRepository<CompraEntity, UUID> {

    /**
     * Busca una compra por su ID.
     */
    Optional<CompraEntity> findById(UUID id);
}
