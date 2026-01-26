package com.budgetpro.infrastructure.persistence.repository.almacen;

import com.budgetpro.infrastructure.persistence.entity.almacen.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para KardexEntity.
 */
@Repository
public interface KardexJpaRepository extends JpaRepository<KardexEntity, UUID> {

    /**
     * Busca todos los registros de Kárdex de un almacén y recurso ordenados por fecha descendente.
     */
    List<KardexEntity> findByAlmacenIdAndRecursoIdOrderByFechaMovimientoDesc(UUID almacenId, UUID recursoId);

    /**
     * Busca el último registro de Kárdex para un almacén y recurso.
     */
    @Query("SELECT k FROM KardexEntity k WHERE k.almacenId = :almacenId AND k.recursoId = :recursoId ORDER BY k.fechaMovimiento DESC, k.createdAt DESC LIMIT 1")
    Optional<KardexEntity> findUltimoPorAlmacenIdYRecursoId(UUID almacenId, UUID recursoId);
}
