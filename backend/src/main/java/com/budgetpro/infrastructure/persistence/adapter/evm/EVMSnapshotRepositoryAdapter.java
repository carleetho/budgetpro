package com.budgetpro.infrastructure.persistence.adapter.evm;

import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;
import com.budgetpro.domain.finanzas.evm.port.out.EVMSnapshotRepository;
import com.budgetpro.infrastructure.persistence.entity.evm.EVMSnapshotEntity;
import com.budgetpro.infrastructure.persistence.mapper.evm.EVMSnapshotMapper;
import com.budgetpro.infrastructure.persistence.repository.evm.JpaEVMSnapshotRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para EVMSnapshotRepository.
 */
@Component
public class EVMSnapshotRepositoryAdapter implements EVMSnapshotRepository {

    private final JpaEVMSnapshotRepository jpaRepository;
    private final EVMSnapshotMapper mapper;

    public EVMSnapshotRepositoryAdapter(JpaEVMSnapshotRepository jpaRepository, EVMSnapshotMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(EVMSnapshot snapshot) {
        EVMSnapshotEntity entity = mapper.toEntity(snapshot);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EVMSnapshot> findLatestByProyectoId(UUID proyectoId) {
        return jpaRepository.findFirstByProyectoIdOrderByFechaCorteDesc(proyectoId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EVMSnapshot> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoIdOrderByFechaCorteDesc(proyectoId).stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EVMSnapshot> findByProyectoIdAndRango(UUID proyectoId, LocalDateTime desde, LocalDateTime hasta) {
        return jpaRepository.findByProyectoIdAndFechaCorteBetweenOrderByFechaCorteDesc(proyectoId, desde, hasta)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProyectoIdAndFechaCorte(UUID proyectoId, LocalDateTime fechaCorte) {
        return jpaRepository.existsByProyectoIdAndFechaCorte(proyectoId, fechaCorte);
    }
}
