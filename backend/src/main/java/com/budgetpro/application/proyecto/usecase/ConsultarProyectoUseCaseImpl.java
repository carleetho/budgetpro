package com.budgetpro.application.proyecto.usecase;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.port.in.ConsultarProyectoUseCase;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.mapper.ProyectoMapper;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementación del caso de uso para consultar un proyecto individual.
 */
@Service
public class ConsultarProyectoUseCaseImpl implements ConsultarProyectoUseCase {

    private final ProyectoMapper proyectoMapper;
    private final ProyectoJpaRepository proyectoJpaRepository;

    public ConsultarProyectoUseCaseImpl(ProyectoMapper proyectoMapper,
                                       ProyectoJpaRepository proyectoJpaRepository) {
        this.proyectoMapper = proyectoMapper;
        this.proyectoJpaRepository = proyectoJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProyectoResponse> obtenerPorId(ProyectoId id) {
        return proyectoJpaRepository.findById(id.getValue())
                .map(proyectoMapper::toResponse);
    }
}
