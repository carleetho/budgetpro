package com.budgetpro.infrastructure.persistence.repository.almacen;

import com.budgetpro.infrastructure.persistence.entity.almacen.MovimientoAlmacenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para MovimientoAlmacenEntity.
 */
@Repository
public interface MovimientoAlmacenJpaRepository extends JpaRepository<MovimientoAlmacenEntity, UUID> {

    /**
     * Busca todos los movimientos de un almacén ordenados por fecha descendente.
     */
    List<MovimientoAlmacenEntity> findByAlmacenIdOrderByFechaMovimientoDesc(UUID almacenId);

    /**
     * Busca todos los movimientos de un recurso en un almacén ordenados por fecha descendente.
     */
    List<MovimientoAlmacenEntity> findByAlmacenIdAndRecursoIdOrderByFechaMovimientoDesc(UUID almacenId, UUID recursoId);
}
