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
 * Proporciona métodos de acceso a datos para la entidad Partida.
 */
@Repository
public interface PartidaJpaRepository extends JpaRepository<PartidaEntity, UUID> {

    /**
     * Busca todas las partidas de un proyecto.
     */
    @Query("SELECT p FROM PartidaEntity p WHERE p.proyectoId = :proyectoId")
    List<PartidaEntity> findByProyectoId(@Param("proyectoId") UUID proyectoId);

    /**
     * Busca todas las partidas de un presupuesto.
     */
    @Query("SELECT p FROM PartidaEntity p WHERE p.presupuesto.id = :presupuestoId")
    List<PartidaEntity> findByPresupuestoId(@Param("presupuestoId") UUID presupuestoId);

    /**
     * Verifica si existe una partida con un código específico en un presupuesto.
     */
    @Query("SELECT COUNT(p) > 0 FROM PartidaEntity p WHERE p.presupuesto.id = :presupuestoId AND p.codigo = :codigo")
    boolean existsByPresupuestoIdAndCodigo(@Param("presupuestoId") UUID presupuestoId, @Param("codigo") String codigo);

    /**
     * Busca una partida por ID.
     */
    Optional<PartidaEntity> findById(UUID id);
}
