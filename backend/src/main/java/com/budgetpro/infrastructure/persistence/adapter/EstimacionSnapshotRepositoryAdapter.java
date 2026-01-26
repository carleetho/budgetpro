package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionSnapshot;
import com.budgetpro.domain.finanzas.estimacion.port.EstimacionSnapshotRepository;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionSnapshotEntity;
import com.budgetpro.infrastructure.persistence.mapper.EstimacionSnapshotMapper;
import com.budgetpro.infrastructure.persistence.repository.EstimacionSnapshotJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EstimacionSnapshotRepositoryAdapter implements EstimacionSnapshotRepository {

    private final EstimacionSnapshotJpaRepository snapshotJpaRepository;
    private final EstimacionSnapshotMapper snapshotMapper;

    public EstimacionSnapshotRepositoryAdapter(EstimacionSnapshotJpaRepository snapshotJpaRepository,
            EstimacionSnapshotMapper snapshotMapper) {
        this.snapshotJpaRepository = snapshotJpaRepository;
        this.snapshotMapper = snapshotMapper;
    }

    @Override
    public void save(EstimacionSnapshot snapshot) {
        EstimacionSnapshotEntity entity = snapshotMapper.toEntity(snapshot);
        snapshotJpaRepository.save(entity);
    }

    @Override
    public Optional<EstimacionSnapshot> findByEstimacionId(EstimacionId estimacionId) {
        return snapshotJpaRepository.findByEstimacionId(estimacionId.getValue()).map(snapshotMapper::toDomain);
    }
}
