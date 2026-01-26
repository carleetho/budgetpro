package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuInsumoSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ApuInsumoSnapshotEntity.
 */
@Repository
public interface ApuInsumoSnapshotJpaRepository extends JpaRepository<ApuInsumoSnapshotEntity, UUID> {

    /**
     * Busca todos los insumos de un APU snapshot específico.
     *
     * @param apuSnapshotId El UUID del APU snapshot.
     * @return Lista de insumos del APU.
     */
    List<ApuInsumoSnapshotEntity> findByApuSnapshotId(UUID apuSnapshotId);

    /**
     * Busca todos los insumos que usan un recurso específico (por external_id).
     *
     * @param recursoExternalId El external_id del recurso.
     * @return Lista de insumos que usan ese recurso.
     */
    List<ApuInsumoSnapshotEntity> findByRecursoExternalId(String recursoExternalId);
}
