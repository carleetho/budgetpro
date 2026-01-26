package com.budgetpro.infrastructure.persistence.adapter.presupuesto;

import com.budgetpro.domain.finanzas.presupuesto.model.IntegrityAuditEntry;
import com.budgetpro.domain.finanzas.presupuesto.port.out.IntegrityAuditRepository;
import com.budgetpro.infrastructure.persistence.mapper.presupuesto.IntegrityAuditMapper;
import com.budgetpro.infrastructure.persistence.repository.presupuesto.IntegrityAuditJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class IntegrityAuditRepositoryAdapter implements IntegrityAuditRepository {

    private final IntegrityAuditJpaRepository jpaRepository;
    private final IntegrityAuditMapper mapper;

    public IntegrityAuditRepositoryAdapter(IntegrityAuditJpaRepository jpaRepository,
                                            IntegrityAuditMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public IntegrityAuditEntry save(IntegrityAuditEntry entry) {
        var entity = mapper.toEntity(entry);
        var savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrityAuditEntry> findByPresupuestoId(UUID presupuestoId) {
        return jpaRepository.findByPresupuestoIdOrderByValidatedAtDesc(presupuestoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrityAuditEntry> findViolations() {
        return jpaRepository.findByEventTypeOrderByValidatedAtDesc("HASH_VIOLATION").stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
