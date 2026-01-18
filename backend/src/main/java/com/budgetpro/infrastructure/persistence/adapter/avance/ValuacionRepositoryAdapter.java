package com.budgetpro.infrastructure.persistence.adapter.avance;

import com.budgetpro.domain.finanzas.avance.model.Valuacion;
import com.budgetpro.domain.finanzas.avance.model.ValuacionId;
import com.budgetpro.domain.finanzas.avance.port.out.ValuacionRepository;
import com.budgetpro.infrastructure.persistence.entity.avance.ValuacionEntity;
import com.budgetpro.infrastructure.persistence.mapper.avance.ValuacionMapper;
import com.budgetpro.infrastructure.persistence.repository.avance.ValuacionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para ValuacionRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ValuacionRepositoryAdapter implements ValuacionRepository {

    private final ValuacionJpaRepository jpaRepository;
    private final ValuacionMapper mapper;

    public ValuacionRepositoryAdapter(ValuacionJpaRepository jpaRepository, ValuacionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Valuacion valuacion) {
        Optional<ValuacionEntity> existingEntityOpt = jpaRepository.findById(valuacion.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            ValuacionEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, valuacion);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            ValuacionEntity newEntity = mapper.toEntity(valuacion);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Valuacion> findById(ValuacionId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Valuacion> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
