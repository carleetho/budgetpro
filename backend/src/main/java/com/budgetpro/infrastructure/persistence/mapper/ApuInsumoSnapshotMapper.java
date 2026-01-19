package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuInsumoSnapshotEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper para convertir entre APUInsumoSnapshot (dominio) y ApuInsumoSnapshotEntity (persistencia).
 */
@Component
public class ApuInsumoSnapshotMapper {

    public ApuInsumoSnapshotEntity toEntity(APUInsumoSnapshot insumo,
                                            ApuSnapshotEntity apuSnapshotEntity,
                                            UUID createdBy) {
        if (insumo == null) {
            return null;
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo");
        }

        ApuInsumoSnapshotEntity entity = new ApuInsumoSnapshotEntity();
        entity.setId(insumo.getId().getValue());
        entity.setApuSnapshot(apuSnapshotEntity);
        entity.setRecursoExternalId(insumo.getRecursoExternalId());
        entity.setRecursoNombre(insumo.getRecursoNombre());
        entity.setCantidad(insumo.getCantidad());
        entity.setPrecioUnitario(insumo.getPrecioUnitario());
        entity.setSubtotal(insumo.getSubtotal());
        entity.setCreatedBy(createdBy);
        return entity;
    }

    public APUInsumoSnapshot toDomain(ApuInsumoSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }

        return APUInsumoSnapshot.reconstruir(
                APUInsumoSnapshotId.of(entity.getId()),
                entity.getRecursoExternalId(),
                entity.getRecursoNombre(),
                entity.getCantidad(),
                entity.getPrecioUnitario(),
                entity.getSubtotal()
        );
    }
}
