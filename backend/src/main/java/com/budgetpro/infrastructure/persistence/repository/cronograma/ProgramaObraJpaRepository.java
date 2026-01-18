package com.budgetpro.infrastructure.persistence.repository.cronograma;

import com.budgetpro.infrastructure.persistence.entity.cronograma.ProgramaObraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ProgramaObraEntity.
 */
@Repository
public interface ProgramaObraJpaRepository extends JpaRepository<ProgramaObraEntity, UUID> {

    /**
     * Busca el programa de obra de un proyecto (relación 1:1).
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con el programa si existe
     */
    Optional<ProgramaObraEntity> findByProyectoId(UUID proyectoId);
}
