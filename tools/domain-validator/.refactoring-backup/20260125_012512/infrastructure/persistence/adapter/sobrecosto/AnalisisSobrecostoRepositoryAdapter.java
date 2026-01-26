package com.budgetpro.infrastructure.persistence.adapter.sobrecosto;

import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecosto;
import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecostoId;
import com.budgetpro.domain.finanzas.sobrecosto.port.out.AnalisisSobrecostoRepository;
import com.budgetpro.infrastructure.persistence.entity.sobrecosto.AnalisisSobrecostoEntity;
import com.budgetpro.infrastructure.persistence.mapper.sobrecosto.AnalisisSobrecostoMapper;
import com.budgetpro.infrastructure.persistence.repository.sobrecosto.AnalisisSobrecostoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para AnalisisSobrecostoRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class AnalisisSobrecostoRepositoryAdapter implements AnalisisSobrecostoRepository {

    private final AnalisisSobrecostoJpaRepository jpaRepository;
    private final AnalisisSobrecostoMapper mapper;

    public AnalisisSobrecostoRepositoryAdapter(AnalisisSobrecostoJpaRepository jpaRepository,
                                               AnalisisSobrecostoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(AnalisisSobrecosto analisis) {
        Optional<AnalisisSobrecostoEntity> existingEntityOpt = jpaRepository.findById(analisis.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            AnalisisSobrecostoEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, analisis);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            AnalisisSobrecostoEntity newEntity = mapper.toEntity(analisis);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnalisisSobrecosto> findById(AnalisisSobrecostoId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AnalisisSobrecosto> findByPresupuestoId(UUID presupuestoId) {
        return jpaRepository.findByPresupuestoId(presupuestoId)
                .map(mapper::toDomain);
    }
}
