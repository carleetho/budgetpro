package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.mapper.PresupuestoMapper;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para PresupuestoRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión. Hibernate maneja el
 * Optimistic Locking automáticamente con @Version.
 */
@Component
public class PresupuestoRepositoryAdapter implements PresupuestoRepository {

    private final PresupuestoJpaRepository jpaRepository;
    private final PresupuestoMapper mapper;

    public PresupuestoRepositoryAdapter(PresupuestoJpaRepository jpaRepository, PresupuestoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Presupuesto presupuesto) {
        Optional<PresupuestoEntity> existingEntityOpt = jpaRepository.findById(presupuesto.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: mapear y guardar
            PresupuestoEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, presupuesto);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            PresupuestoEntity newEntity = mapper.toEntity(presupuesto);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Presupuesto> findById(PresupuestoId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Presupuesto> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Presupuesto> findActiveByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId) // Assuming finding by ID returns the active one or unique per
                                                          // project?
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProyectoId(UUID proyectoId) {
        return jpaRepository.existsByProyectoId(proyectoId);
    }
}
