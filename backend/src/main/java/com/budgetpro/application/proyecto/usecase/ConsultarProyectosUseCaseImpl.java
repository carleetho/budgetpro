package com.budgetpro.application.proyecto.usecase;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.port.in.ConsultarProyectosUseCase;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.mapper.ProyectoMapper;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación del caso de uso para consultar proyectos.
 */
@Service
public class ConsultarProyectosUseCaseImpl implements ConsultarProyectosUseCase {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoMapper proyectoMapper;
    private final ProyectoJpaRepository proyectoJpaRepository;

    public ConsultarProyectosUseCaseImpl(ProyectoRepository proyectoRepository, 
                                         ProyectoMapper proyectoMapper,
                                         ProyectoJpaRepository proyectoJpaRepository) {
        this.proyectoRepository = proyectoRepository;
        this.proyectoMapper = proyectoMapper;
        this.proyectoJpaRepository = proyectoJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listar() {
        // Obtener entidades directamente para tener acceso a createdAt/updatedAt
        return proyectoJpaRepository.findAll().stream()
                .map(entity -> {
                    com.budgetpro.domain.proyecto.model.Proyecto proyecto = proyectoMapper.toDomain(entity);
                    return new ProyectoResponse(
                            proyecto.getId().getValue(),
                            proyecto.getNombre(),
                            proyecto.getUbicacion(),
                            proyecto.getEstado(),
                            entity.getCreatedAt(),
                            entity.getUpdatedAt()
                    );
                })
                .toList();
    }
}
