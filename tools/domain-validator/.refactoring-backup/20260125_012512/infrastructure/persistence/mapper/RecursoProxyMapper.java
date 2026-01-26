package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.infrastructure.persistence.entity.catalogo.RecursoProxyEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper para convertir entre RecursoProxy (dominio) y RecursoProxyEntity
 * (persistencia).
 */
@Component
public class RecursoProxyMapper {

    public RecursoProxyEntity toEntity(RecursoProxy recursoProxy, UUID createdBy) {
        if (recursoProxy == null) {
            return null;
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo");
        }

        RecursoProxyEntity entity = new RecursoProxyEntity();
        entity.setId(recursoProxy.getId().getValue());
        entity.setExternalId(recursoProxy.getExternalId());
        entity.setCatalogSource(recursoProxy.getCatalogSource());
        entity.setNombreSnapshot(recursoProxy.getNombreSnapshot());
        entity.setTipoSnapshot(recursoProxy.getTipoSnapshot());
        entity.setUnidadSnapshot(recursoProxy.getUnidadSnapshot());
        entity.setPrecioSnapshot(recursoProxy.getPrecioSnapshot());
        entity.setSnapshotDate(recursoProxy.getSnapshotDate());
        entity.setEstado(recursoProxy.getEstado());
        entity.setCostoReal(recursoProxy.getCostoReal());
        entity.setVersion(recursoProxy.getVersion());
        entity.setCreatedBy(createdBy);
        return entity;
    }

    public RecursoProxy toDomain(RecursoProxyEntity entity) {
        if (entity == null) {
            return null;
        }

        return RecursoProxy.reconstruir(RecursoProxyId.of(entity.getId()), entity.getExternalId(),
                entity.getCatalogSource(), entity.getNombreSnapshot(), entity.getTipoSnapshot(),
                entity.getUnidadSnapshot(), entity.getPrecioSnapshot(), entity.getSnapshotDate(), entity.getEstado(),
                entity.getCostoReal(), entity.getVersion());
    }
}
