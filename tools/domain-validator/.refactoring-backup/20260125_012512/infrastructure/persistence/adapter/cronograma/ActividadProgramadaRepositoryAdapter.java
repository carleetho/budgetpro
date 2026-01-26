package com.budgetpro.infrastructure.persistence.adapter.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramadaId;
import com.budgetpro.domain.finanzas.cronograma.port.out.ActividadProgramadaRepository;
import com.budgetpro.infrastructure.persistence.entity.cronograma.ActividadProgramadaEntity;
import com.budgetpro.infrastructure.persistence.mapper.cronograma.ActividadProgramadaMapper;
import com.budgetpro.infrastructure.persistence.repository.cronograma.ActividadProgramadaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para ActividadProgramadaRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ActividadProgramadaRepositoryAdapter implements ActividadProgramadaRepository {

    private final ActividadProgramadaJpaRepository jpaRepository;
    private final ActividadProgramadaMapper mapper;

    public ActividadProgramadaRepositoryAdapter(ActividadProgramadaJpaRepository jpaRepository,
                                               ActividadProgramadaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(ActividadProgramada actividad) {
        Optional<ActividadProgramadaEntity> existingEntityOpt = jpaRepository.findById(actividad.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            ActividadProgramadaEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, actividad);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            ActividadProgramadaEntity newEntity = mapper.toEntity(actividad);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActividadProgramada> findById(ActividadProgramadaId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadProgramada> findByProgramaObraId(UUID programaObraId) {
        return jpaRepository.findByProgramaObraId(programaObraId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActividadProgramada> findByPartidaId(UUID partidaId) {
        return jpaRepository.findByPartidaId(partidaId)
                .map(mapper::toDomain);
    }
}
