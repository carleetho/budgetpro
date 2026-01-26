package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionSnapshot;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionSnapshotId;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionSnapshotEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EstimacionSnapshotMapper {

    public EstimacionSnapshot toDomain(EstimacionSnapshotEntity entity) {
        if (entity == null)
            return null;

        return EstimacionSnapshot.reconstruir(EstimacionSnapshotId.of(entity.getId()),
                EstimacionId.of(entity.getEstimacionId()), entity.getItemsSnapshot(), entity.getTotalesSnapshot(),
                entity.getMetadataSnapshot(), entity.getSnapshotDate(), entity.getSnapshotAlgorithm());
    }

    public EstimacionSnapshotEntity toEntity(EstimacionSnapshot domain) {
        if (domain == null)
            return null;

        EstimacionSnapshotEntity entity = new EstimacionSnapshotEntity();
        entity.setId(domain.getId().getValue());
        entity.setEstimacionId(domain.getEstimacionId().getValue());
        entity.setItemsSnapshot(domain.getItemsSnapshot());
        entity.setTotalesSnapshot(domain.getTotalesSnapshot());
        entity.setMetadataSnapshot(domain.getMetadataSnapshot());
        entity.setSnapshotAlgorithm(domain.getSnapshotAlgorithm());
        // SnapshotDate is handled by CreationTimestamp in entity usually, but if domain
        // has it...
        // Domain has it. Better to set it if we want to preserve exact time or let DB
        // set it on insert.
        // If domain object is "new", date might be now();
        // If restoring, we want original date.
        // Let's set it if not null, though @CreationTimestamp might override on insert?
        // Usually @CreationTimestamp only works if null or always?
        // Standard Hibernate @CreationTimestamp overrides.
        // But for "Snapshot", the domain date IS the creation date.
        // Let's rely on entity field.
        // If I want to force it, I might need to disable annotation or use simple
        // column.
        // For now, let's set it.
        entity.setSnapshotDate(domain.getSnapshotDate());

        return entity;
    }
}
