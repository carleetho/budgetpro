package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApuSnapshotMapperTest {

        private final ApuSnapshotMapper mapper = new ApuSnapshotMapper(
                        new ApuInsumoSnapshotMapper(new ComposicionCuadrillaSnapshotMapper()));

        @Test
        void toEntity_y_toDomain_debenPreservarDatos() {
                APUSnapshot snapshot = APUSnapshot.crear(APUSnapshotId.generate(), UUID.randomUUID(), "APU-EXT-1",
                                "CATALOGO-A", new BigDecimal("1.5"), "UND", LocalDateTime.now());
                snapshot = snapshot.agregarInsumo(APUInsumoSnapshot.crear(APUInsumoSnapshotId.generate(), "EXT-REC-1",
                                "Arena", new BigDecimal("1.5"), new BigDecimal("50.00")));

                PartidaEntity partidaEntity = new PartidaEntity();
                partidaEntity.setId(snapshot.getPartidaId());
                UUID createdBy = UUID.randomUUID();

                ApuSnapshotEntity entity = mapper.toEntity(snapshot, partidaEntity, createdBy);

                assertEquals(snapshot.getId().getValue(), entity.getId());
                assertEquals(snapshot.getExternalApuId(), entity.getExternalApuId());
                assertEquals(snapshot.getCatalogSource(), entity.getCatalogSource());
                assertEquals(snapshot.getRendimientoOriginal(), entity.getRendimientoOriginal());
                assertEquals(snapshot.getRendimientoVigente(), entity.getRendimientoVigente());
                assertEquals(snapshot.getUnidadSnapshot(), entity.getUnidadSnapshot());
                assertEquals(snapshot.getSnapshotDate(), entity.getSnapshotDate());
                assertNotNull(entity.getInsumos());
                assertEquals(1, entity.getInsumos().size());
                assertEquals(createdBy, entity.getCreatedBy());

                APUSnapshot roundtrip = mapper.toDomain(entity);
                assertEquals(snapshot.getId(), roundtrip.getId());
                assertEquals(snapshot.getExternalApuId(), roundtrip.getExternalApuId());
                assertEquals(snapshot.getCatalogSource(), roundtrip.getCatalogSource());
                assertEquals(snapshot.getRendimientoOriginal(), roundtrip.getRendimientoOriginal());
                assertEquals(snapshot.getRendimientoVigente(), roundtrip.getRendimientoVigente());
                assertEquals(snapshot.getUnidadSnapshot(), roundtrip.getUnidadSnapshot());
                assertEquals(1, roundtrip.getInsumos().size());
        }
}
