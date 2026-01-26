package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad RecursoEntity.
 * Proporciona operaciones de persistencia básicas.
 */
@Repository
public interface RecursoJpaRepository extends JpaRepository<RecursoEntity, UUID> {

    /**
     * Verifica si existe un recurso con el nombre normalizado dado.
     * 
     * @param nombreNormalizado El nombre normalizado a buscar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreNormalizado(String nombreNormalizado);

    /**
     * Busca un recurso por su nombre normalizado.
     * 
     * @param nombreNormalizado El nombre normalizado a buscar
     * @return Un Optional con el recurso si existe
     */
    Optional<RecursoEntity> findByNombreNormalizado(String nombreNormalizado);
}
