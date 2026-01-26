package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ApuSnapshotEntity.
 */
@Repository
public interface ApuSnapshotJpaRepository extends JpaRepository<ApuSnapshotEntity, UUID> {

    Optional<ApuSnapshotEntity> findByPartidaId(UUID partidaId);

    List<ApuSnapshotEntity> findByRendimientoModificadoTrue();
}
