package com.budgetpro.infrastructure.persistence.adapter.alertas;

import com.budgetpro.domain.finanzas.alertas.model.AnalisisPresupuesto;
import com.budgetpro.domain.finanzas.alertas.port.out.AnalisisPresupuestoRepository;
import com.budgetpro.infrastructure.persistence.entity.alertas.AnalisisPresupuestoEntity;
import com.budgetpro.infrastructure.persistence.mapper.alertas.AnalisisPresupuestoMapper;
import com.budgetpro.infrastructure.persistence.repository.alertas.AnalisisPresupuestoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para AnalisisPresupuestoRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class AnalisisPresupuestoRepositoryAdapter implements AnalisisPresupuestoRepository {

    private final AnalisisPresupuestoJpaRepository jpaRepository;
    private final AnalisisPresupuestoMapper mapper;

    public AnalisisPresupuestoRepositoryAdapter(AnalisisPresupuestoJpaRepository jpaRepository,
                                                AnalisisPresupuestoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void guardar(AnalisisPresupuesto analisis) {
        Optional<AnalisisPresupuestoEntity> existingEntityOpt = jpaRepository.findById(analisis.getId());
        
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            AnalisisPresupuestoEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, analisis);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            AnalisisPresupuestoEntity newEntity = mapper.toEntity(analisis);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnalisisPresupuesto> buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnalisisPresupuesto> buscarUltimoPorPresupuestoId(UUID presupuestoId) {
        return jpaRepository.findUltimoPorPresupuestoId(presupuestoId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalisisPresupuesto> buscarTodosPorPresupuestoId(UUID presupuestoId) {
        return jpaRepository.findByPresupuestoIdOrderByFechaAnalisisDesc(presupuestoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
