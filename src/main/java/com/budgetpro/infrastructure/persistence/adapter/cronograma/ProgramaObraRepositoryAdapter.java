package com.budgetpro.infrastructure.persistence.adapter.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.infrastructure.persistence.entity.cronograma.ProgramaObraEntity;
import com.budgetpro.infrastructure.persistence.mapper.cronograma.ProgramaObraMapper;
import com.budgetpro.infrastructure.persistence.repository.cronograma.ProgramaObraJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para ProgramaObraRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ProgramaObraRepositoryAdapter implements ProgramaObraRepository {

    private final ProgramaObraJpaRepository jpaRepository;
    private final ProgramaObraMapper mapper;

    public ProgramaObraRepositoryAdapter(ProgramaObraJpaRepository jpaRepository,
                                        ProgramaObraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(ProgramaObra programaObra) {
        Optional<ProgramaObraEntity> existingEntityOpt = jpaRepository.findById(programaObra.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            ProgramaObraEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, programaObra);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            ProgramaObraEntity newEntity = mapper.toEntity(programaObra);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramaObra> findById(ProgramaObraId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProgramaObra> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId)
                .map(mapper::toDomain);
    }
}
