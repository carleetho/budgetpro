package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository para PartidaEntity.
 * 
 * Proporciona métodos de acceso a datos para la entidad Partida,
 * incluyendo queries JPQL personalizadas para consultas por proyecto y presupuesto.
 */
@Repository
public interface PartidaJpaRepository extends JpaRepository<PartidaEntity, UUID> {

    /**
     * Busca todas las partidas de un proyecto.
     * Query JPQL usando la relación presupuesto -> proyectoId.
     */
    @Query("SELECT p FROM PartidaEntity p WHERE p.presupuesto.proyectoId = :proyectoId")
    List<PartidaEntity> findByProyectoId(@Param("proyectoId") UUID proyectoId);

    /**
     * Busca todas las partidas de un presupuesto.
     * Query JPQL usando la relación bidireccional presupuesto.
     */
    @Query("SELECT p FROM PartidaEntity p WHERE p.presupuesto.id = :presupuestoId")
    List<PartidaEntity> findByPresupuestoId(@Param("presupuestoId") UUID presupuestoId);

    /**
     * Verifica si existe una partida con el código especificado en el presupuesto dado.
     * Query JPQL usando la relación bidireccional presupuesto.
     */
    @Query("SELECT COUNT(p) > 0 FROM PartidaEntity p WHERE p.presupuesto.id = :presupuestoId AND p.codigo = :codigo")
    boolean existsByPresupuestoIdAndCodigo(@Param("presupuestoId") UUID presupuestoId, @Param("codigo") String codigo);

    /**
     * Busca una partida por su ID.
     */
    Optional<PartidaEntity> findById(UUID id);
}
