package com.budgetpro.infrastructure.persistence.adapter.almacen;

import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacenId;
import com.budgetpro.domain.logistica.almacen.port.out.MovimientoAlmacenRepository;
import com.budgetpro.infrastructure.persistence.entity.almacen.MovimientoAlmacenEntity;
import com.budgetpro.infrastructure.persistence.mapper.almacen.MovimientoAlmacenMapper;
import com.budgetpro.infrastructure.persistence.repository.almacen.MovimientoAlmacenJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para MovimientoAlmacenRepository.
 */
@Component
public class MovimientoAlmacenRepositoryAdapter implements MovimientoAlmacenRepository {

    private final MovimientoAlmacenJpaRepository jpaRepository;
    private final MovimientoAlmacenMapper mapper;

    public MovimientoAlmacenRepositoryAdapter(MovimientoAlmacenJpaRepository jpaRepository,
                                              MovimientoAlmacenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void guardar(MovimientoAlmacen movimiento) {
        Optional<MovimientoAlmacenEntity> existingEntityOpt = jpaRepository.findById(movimiento.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            // Los movimientos son inmutables después de crear, solo se pueden crear nuevos
            throw new IllegalStateException("No se puede actualizar un movimiento de almacén existente");
        } else {
            MovimientoAlmacenEntity newEntity = mapper.toEntity(movimiento);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MovimientoAlmacen> buscarPorId(MovimientoAlmacenId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoAlmacen> buscarPorAlmacenId(UUID almacenId) {
        return jpaRepository.findByAlmacenIdOrderByFechaMovimientoDesc(almacenId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoAlmacen> buscarPorAlmacenIdYRecursoId(UUID almacenId, UUID recursoId) {
        return jpaRepository.findByAlmacenIdAndRecursoIdOrderByFechaMovimientoDesc(almacenId, recursoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
