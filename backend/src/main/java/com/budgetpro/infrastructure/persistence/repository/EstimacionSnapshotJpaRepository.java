package com.budgetpro.infrastructure.persistence.repository;

import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstimacionSnapshotJpaRepository extends JpaRepository<EstimacionSnapshotEntity, UUID> {
    Optional<EstimacionSnapshotEntity> findByEstimacionId(UUID estimacionId);
}
