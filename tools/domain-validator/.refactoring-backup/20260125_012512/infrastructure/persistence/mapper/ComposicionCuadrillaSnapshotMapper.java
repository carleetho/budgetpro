package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.ComposicionCuadrillaSnapshot;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuInsumoSnapshotEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ComposicionCuadrillaSnapshotEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre ComposicionCuadrillaSnapshot (dominio) y ComposicionCuadrillaSnapshotEntity (persistencia).
 */
@Component
public class ComposicionCuadrillaSnapshotMapper {

    public ComposicionCuadrillaSnapshotEntity toEntity(ComposicionCuadrillaSnapshot composicion,
                                                       ApuInsumoSnapshotEntity apuInsumoSnapshotEntity,
                                                       UUID id,
                                                       UUID createdBy) {
        if (composicion == null) {
            return null;
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo");
        }

        ComposicionCuadrillaSnapshotEntity entity = new ComposicionCuadrillaSnapshotEntity();
        entity.setId(id);
        entity.setApuInsumoSnapshot(apuInsumoSnapshotEntity);
        entity.setPersonalExternalId(composicion.personalExternalId());
        entity.setPersonalNombre(composicion.personalNombre());
        entity.setCantidad(composicion.cantidad());
        entity.setCostoDia(composicion.costoDia());
        entity.setMoneda(composicion.moneda());
        entity.setCreatedBy(createdBy);
        return entity;
    }

    public ComposicionCuadrillaSnapshot toDomain(ComposicionCuadrillaSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ComposicionCuadrillaSnapshot(
                entity.getPersonalExternalId(),
                entity.getPersonalNombre(),
                entity.getCantidad(),
                entity.getCostoDia(),
                entity.getMoneda()
        );
    }

    public List<ComposicionCuadrillaSnapshot> toDomainList(List<ComposicionCuadrillaSnapshotEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
