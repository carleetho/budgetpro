package com.budgetpro.infrastructure.persistence.adapter.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.cronograma.CronogramaSnapshotEntity;
import com.budgetpro.infrastructure.persistence.mapper.cronograma.CronogramaSnapshotMapper;
import com.budgetpro.infrastructure.persistence.repository.cronograma.CronogramaSnapshotJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para CronogramaSnapshotRepository.
 */
@Component
public class CronogramaSnapshotRepositoryAdapter implements CronogramaSnapshotRepository {

    private final CronogramaSnapshotJpaRepository jpaRepository;
    private final CronogramaSnapshotMapper mapper;

    public CronogramaSnapshotRepositoryAdapter(CronogramaSnapshotJpaRepository jpaRepository,
                                               CronogramaSnapshotMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(CronogramaSnapshot snapshot) {
        CronogramaSnapshotEntity entity = mapper.toEntity(snapshot);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CronogramaSnapshot> findById(CronogramaSnapshotId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CronogramaSnapshot> findByProgramaObraId(ProgramaObraId programaObraId) {
        return jpaRepository.findFirstByProgramaObraIdOrderBySnapshotDateDesc(programaObraId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CronogramaSnapshot> findByPresupuestoId(PresupuestoId presupuestoId) {
        return jpaRepository.findByPresupuestoIdOrderBySnapshotDateDesc(presupuestoId.getValue())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
