package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ProyectoEntity.
 */
@Repository
public interface ProyectoJpaRepository extends JpaRepository<ProyectoEntity, UUID> {

    /**
     * Busca un proyecto por su nombre.
     * 
     * @param nombre El nombre del proyecto
     * @return Optional con el proyecto si existe
     */
    Optional<ProyectoEntity> findByNombre(String nombre);

    /**
     * Verifica si existe un proyecto con el nombre dado.
     * 
     * @param nombre El nombre del proyecto
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);
}
