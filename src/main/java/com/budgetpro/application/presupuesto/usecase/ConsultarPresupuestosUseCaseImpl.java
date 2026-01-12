package com.budgetpro.application.presupuesto.usecase;

import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;
import com.budgetpro.application.presupuesto.port.in.ConsultarPresupuestosUseCase;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementación del caso de uso para consultar presupuestos.
 * 
 * Responsabilidades:
 * - Orquestar la consulta de presupuestos desde la base de datos
 * - Mapear entidades JPA a DTOs de respuesta
 * 
 * NO contiene lógica de negocio profunda (solo lectura).
 */
@Service
public class ConsultarPresupuestosUseCaseImpl implements ConsultarPresupuestosUseCase {

    private final PresupuestoJpaRepository presupuestoJpaRepository;

    public ConsultarPresupuestosUseCaseImpl(PresupuestoJpaRepository presupuestoJpaRepository) {
        this.presupuestoJpaRepository = presupuestoJpaRepository;
    }

    @Override
    public List<PresupuestoResponse> consultarPorProyecto(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }

        List<PresupuestoEntity> entities = presupuestoJpaRepository.findByProyectoId(proyectoId);
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }

    private PresupuestoResponse toResponse(PresupuestoEntity entity) {
        return new PresupuestoResponse(
                entity.getId(),
                entity.getProyectoId(),
                entity.getEsContractual(),
                entity.getVersion()
        );
    }
}
