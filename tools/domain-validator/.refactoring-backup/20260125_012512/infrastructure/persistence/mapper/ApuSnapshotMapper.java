package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuInsumoSnapshotEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre APUSnapshot (dominio) y ApuSnapshotEntity (persistencia).
 */
@Component
public class ApuSnapshotMapper {

    private final ApuInsumoSnapshotMapper insumoMapper;

    public ApuSnapshotMapper(ApuInsumoSnapshotMapper insumoMapper) {
        this.insumoMapper = insumoMapper;
    }

    public ApuSnapshotEntity toEntity(APUSnapshot snapshot, PartidaEntity partidaEntity, UUID createdBy) {
        if (snapshot == null) {
            return null;
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo");
        }

        ApuSnapshotEntity entity = new ApuSnapshotEntity();
        entity.setId(snapshot.getId().getValue());
        entity.setPartida(partidaEntity);
        entity.setExternalApuId(snapshot.getExternalApuId());
        entity.setCatalogSource(snapshot.getCatalogSource());
        entity.setRendimientoOriginal(snapshot.getRendimientoOriginal());
        entity.setRendimientoVigente(snapshot.getRendimientoVigente());
        entity.setRendimientoModificado(snapshot.isRendimientoModificado());
        entity.setRendimientoModificadoPor(snapshot.getRendimientoModificadoPor());
        entity.setRendimientoModificadoEn(snapshot.getRendimientoModificadoEn());
        entity.setUnidadSnapshot(snapshot.getUnidadSnapshot());
        entity.setSnapshotDate(snapshot.getSnapshotDate());
        entity.setVersion(snapshot.getVersion());
        entity.setCreatedBy(createdBy);

        List<ApuInsumoSnapshotEntity> insumos = snapshot.getInsumos().stream()
                .map(insumo -> insumoMapper.toEntity(insumo, entity, createdBy))
                .collect(Collectors.toList());
        entity.setInsumos(insumos);

        return entity;
    }

    public APUSnapshot toDomain(ApuSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }

        List<APUInsumoSnapshot> insumos = entity.getInsumos().stream()
                .map(insumoMapper::toDomain)
                .collect(Collectors.toList());
        UUID partidaId = entity.getPartida() != null ? entity.getPartida().getId() : null;

        return APUSnapshot.reconstruir(
                APUSnapshotId.of(entity.getId()),
                partidaId,
                entity.getExternalApuId(),
                entity.getCatalogSource(),
                entity.getRendimientoOriginal(),
                entity.getRendimientoVigente(),
                Boolean.TRUE.equals(entity.getRendimientoModificado()),
                entity.getRendimientoModificadoPor(),
                entity.getRendimientoModificadoEn(),
                entity.getUnidadSnapshot(),
                entity.getSnapshotDate(),
                insumos,
                entity.getVersion() != null ? entity.getVersion() : 0L
        );
    }
}
