package com.budgetpro.infrastructure.persistence.adapter.sobrecosto;

import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboralId;
import com.budgetpro.domain.finanzas.sobrecosto.port.out.ConfiguracionLaboralRepository;
import com.budgetpro.infrastructure.persistence.entity.sobrecosto.ConfiguracionLaboralEntity;
import com.budgetpro.infrastructure.persistence.mapper.sobrecosto.ConfiguracionLaboralMapper;
import com.budgetpro.infrastructure.persistence.repository.sobrecosto.ConfiguracionLaboralJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para ConfiguracionLaboralRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ConfiguracionLaboralRepositoryAdapter implements ConfiguracionLaboralRepository {

    private final ConfiguracionLaboralJpaRepository jpaRepository;
    private final ConfiguracionLaboralMapper mapper;

    public ConfiguracionLaboralRepositoryAdapter(ConfiguracionLaboralJpaRepository jpaRepository,
                                                ConfiguracionLaboralMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(ConfiguracionLaboral configuracion) {
        Optional<ConfiguracionLaboralEntity> existingEntityOpt = jpaRepository.findById(configuracion.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            ConfiguracionLaboralEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, configuracion);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            ConfiguracionLaboralEntity newEntity = mapper.toEntity(configuracion);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionLaboral> findById(ConfiguracionLaboralId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionLaboral> findGlobal() {
        return jpaRepository.findGlobal()
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfiguracionLaboral> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId)
                .map(mapper::toDomain);
    }
}
