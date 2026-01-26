package com.budgetpro.infrastructure.persistence.adapter.almacen;

import com.budgetpro.domain.logistica.almacen.model.Almacen;
import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.domain.logistica.almacen.port.out.AlmacenRepository;
import com.budgetpro.infrastructure.persistence.entity.almacen.AlmacenEntity;
import com.budgetpro.infrastructure.persistence.mapper.almacen.AlmacenMapper;
import com.budgetpro.infrastructure.persistence.repository.almacen.AlmacenJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para AlmacenRepository.
 */
@Component
public class AlmacenRepositoryAdapter implements AlmacenRepository {

    private final AlmacenJpaRepository jpaRepository;
    private final AlmacenMapper mapper;

    public AlmacenRepositoryAdapter(AlmacenJpaRepository jpaRepository, AlmacenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void guardar(Almacen almacen) {
        Optional<AlmacenEntity> existingEntityOpt = jpaRepository.findById(almacen.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            AlmacenEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, almacen);
            jpaRepository.save(existingEntity);
        } else {
            AlmacenEntity newEntity = mapper.toEntity(almacen);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Almacen> buscarPorId(AlmacenId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Almacen> buscarActivosPorProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoIdAndActivoTrue(proyectoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
