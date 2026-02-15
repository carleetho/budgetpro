package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.AsignacionActividadRepositoryPort;
import com.budgetpro.domain.rrhh.model.AsignacionActividad;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsignacionActividadEntity;
import com.budgetpro.infrastructure.persistence.mapper.rrhh.AsignacionActividadMapper;
import com.budgetpro.infrastructure.persistence.repository.rrhh.AsignacionActividadJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AsignacionActividadRepositoryAdapter implements AsignacionActividadRepositoryPort {

    private final AsignacionActividadJpaRepository jpaRepository;
    private final AsignacionActividadMapper mapper;

    public AsignacionActividadRepositoryAdapter(AsignacionActividadJpaRepository jpaRepository,
                                                AsignacionActividadMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AsignacionActividad save(AsignacionActividad asignacion) {
        AsignacionActividadEntity entity = mapper.toEntity(asignacion);
        AsignacionActividadEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
