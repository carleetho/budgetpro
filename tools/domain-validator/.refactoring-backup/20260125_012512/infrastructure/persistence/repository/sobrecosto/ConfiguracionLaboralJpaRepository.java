package com.budgetpro.infrastructure.persistence.repository.sobrecosto;

import com.budgetpro.infrastructure.persistence.entity.sobrecosto.ConfiguracionLaboralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ConfiguracionLaboralEntity.
 */
@Repository
public interface ConfiguracionLaboralJpaRepository extends JpaRepository<ConfiguracionLaboralEntity, UUID> {

    /**
     * Busca la configuración laboral global (proyectoId IS NULL).
     * 
     * @return Optional con la configuración global si existe
     */
    @Query("SELECT c FROM ConfiguracionLaboralEntity c WHERE c.proyectoId IS NULL")
    Optional<ConfiguracionLaboralEntity> findGlobal();

    /**
     * Busca la configuración laboral de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Optional con la configuración del proyecto si existe
     */
    Optional<ConfiguracionLaboralEntity> findByProyectoId(UUID proyectoId);
}
