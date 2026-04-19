package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.SubpresupuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia SUBPRESUPUESTO (Opción B).
 */
@Repository
public interface SubpresupuestoJpaRepository extends JpaRepository<SubpresupuestoEntity, UUID> {

    Optional<SubpresupuestoEntity> findByPresupuesto_IdAndNombre(UUID presupuestoId, String nombre);
}
