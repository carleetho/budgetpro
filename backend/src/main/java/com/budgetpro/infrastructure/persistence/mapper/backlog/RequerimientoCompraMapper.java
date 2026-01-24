package com.budgetpro.infrastructure.persistence.mapper.backlog;

import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompra;
import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompraId;
import com.budgetpro.infrastructure.persistence.entity.backlog.RequerimientoCompraEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre RequerimientoCompra (dominio) y RequerimientoCompraEntity (persistencia).
 */
@Component
public class RequerimientoCompraMapper {

    /**
     * Convierte un RequerimientoCompra (dominio) a RequerimientoCompraEntity (persistencia).
     */
    public RequerimientoCompraEntity toEntity(RequerimientoCompra requerimiento) {
        if (requerimiento == null) {
            return null;
        }

        return new RequerimientoCompraEntity(
            requerimiento.getId().getValue(),
            requerimiento.getProyectoId(),
            requerimiento.getRequisicionId().getValue(),
            requerimiento.getRecursoExternalId(),
            requerimiento.getCantidadNecesaria(),
            requerimiento.getUnidadMedida(),
            requerimiento.getPrioridad(),
            requerimiento.getEstado(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un RequerimientoCompraEntity (persistencia) a RequerimientoCompra (dominio).
     */
    public RequerimientoCompra toDomain(RequerimientoCompraEntity entity) {
        if (entity == null) {
            return null;
        }

        return RequerimientoCompra.reconstruir(
            RequerimientoCompraId.from(entity.getId()),
            entity.getProyectoId(),
            com.budgetpro.domain.logistica.requisicion.model.RequisicionId.from(entity.getRequisicionId()),
            entity.getRecursoExternalId(),
            entity.getCantidadNecesaria(),
            entity.getUnidadMedida(),
            entity.getPrioridad(),
            entity.getEstado(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Convierte una lista de entidades a dominio.
     */
    public List<RequerimientoCompra> toDomainList(List<RequerimientoCompraEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     */
    public void updateEntity(RequerimientoCompraEntity existingEntity, RequerimientoCompra requerimiento) {
        existingEntity.setEstado(requerimiento.getEstado());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se tocan campos inmutables: proyectoId, requisicionId, recursoExternalId, cantidadNecesaria, unidadMedida, prioridad
    }
}
