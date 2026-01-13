package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.mapper.ProyectoMapper;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador de persistencia para ProyectoRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ProyectoRepositoryAdapter implements ProyectoRepository {

    private final ProyectoJpaRepository jpaRepository;
    private final ProyectoMapper mapper;

    public ProyectoRepositoryAdapter(ProyectoJpaRepository jpaRepository, ProyectoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Proyecto proyecto) {
        Optional<ProyectoEntity> existingEntityOpt = jpaRepository.findById(proyecto.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            ProyectoEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, proyecto);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            ProyectoEntity newEntity = mapper.toEntity(proyecto);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proyecto> findById(ProyectoId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Proyecto> findByNombre(String nombre) {
        return jpaRepository.findByNombre(nombre)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNombre(String nombre) {
        return jpaRepository.existsByNombre(nombre);
    }
}
