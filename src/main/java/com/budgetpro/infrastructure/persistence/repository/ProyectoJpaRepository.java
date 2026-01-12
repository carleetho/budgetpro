package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository para ProyectoEntity.
 * 
 * Proporciona métodos de acceso a datos para la entidad Proyecto.
 * Usado SOLO para lectura (Query Side) según CQRS-Lite.
 */
@Repository
public interface ProyectoJpaRepository extends JpaRepository<ProyectoEntity, UUID> {

    /**
     * Busca todos los proyectos.
     * 
     * @return Lista de todos los proyectos
     */
    List<ProyectoEntity> findAll();

    /**
     * Busca proyectos por estado.
     * 
     * @param estado El estado del proyecto
     * @return Lista de proyectos con el estado especificado
     */
    List<ProyectoEntity> findByEstado(String estado);
}
