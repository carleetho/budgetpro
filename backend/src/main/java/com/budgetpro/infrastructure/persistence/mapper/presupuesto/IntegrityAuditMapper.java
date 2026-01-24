package com.budgetpro.infrastructure.persistence.mapper.presupuesto;

import com.budgetpro.domain.finanzas.presupuesto.model.IntegrityAuditEntry;
import com.budgetpro.infrastructure.persistence.entity.presupuesto.IntegrityAuditEntity;
import org.springframework.stereotype.Component;

@Component
public class IntegrityAuditMapper {

    public IntegrityAuditEntity toEntity(IntegrityAuditEntry entry) {
        return new IntegrityAuditEntity(
            entry.getId(),
            entry.getPresupuestoId(),
            entry.getEventType(),
            entry.getHashApproval(),
            entry.getHashExecution(),
            entry.getValidatedBy(),
            entry.getValidatedAt(),
            entry.getValidationResult(),
            entry.getViolationDetails(),
            entry.getAlgorithmVersion()
        );
    }

    public IntegrityAuditEntry toDomain(IntegrityAuditEntity entity) {
        return IntegrityAuditEntry.crear(
            entity.getId(),
            entity.getPresupuestoId(),
            entity.getEventType(),
            entity.getHashApproval(),
            entity.getHashExecution(),
            entity.getValidatedBy(),
            entity.getValidatedAt(),
            entity.getValidationResult(),
            entity.getViolationDetails(),
            entity.getAlgorithmVersion()
        );
    }
}
