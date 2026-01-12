package com.budgetpro.application.proyecto.usecase;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.application.proyecto.port.in.ConsultarProyectosUseCase;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del caso de uso para consultar proyectos.
 * 
 * Responsabilidades:
 * - Orquestar la consulta de proyectos desde la base de datos
 * - Mapear entidades JPA a DTOs de respuesta
 * 
 * NO contiene lógica de negocio profunda (solo lectura).
 */
@Service
public class ConsultarProyectosUseCaseImpl implements ConsultarProyectosUseCase {

    private final ProyectoJpaRepository proyectoJpaRepository;

    public ConsultarProyectosUseCaseImpl(ProyectoJpaRepository proyectoJpaRepository) {
        this.proyectoJpaRepository = proyectoJpaRepository;
    }

    @Override
    public List<ProyectoResponse> consultarTodos() {
        List<ProyectoEntity> entities = proyectoJpaRepository.findAll();
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProyectoResponse> consultarPorEstado(String estado) {
        List<ProyectoEntity> entities = estado != null 
                ? proyectoJpaRepository.findByEstado(estado)
                : proyectoJpaRepository.findAll();
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    private ProyectoResponse toResponse(ProyectoEntity entity) {
        return new ProyectoResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getEstado()
        );
    }
}
