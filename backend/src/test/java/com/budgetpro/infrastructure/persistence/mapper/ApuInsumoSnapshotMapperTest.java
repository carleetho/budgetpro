package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuInsumoSnapshotEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApuInsumoSnapshotMapperTest {

    private final ApuInsumoSnapshotMapper mapper = new ApuInsumoSnapshotMapper(
            new ComposicionCuadrillaSnapshotMapper());

    @Test
    void toEntity_y_toDomain_debenPreservarDatos() {
        APUInsumoSnapshot domain = APUInsumoSnapshot.crear(APUInsumoSnapshotId.generate(), "EXT-REC-1", "Cemento Gris",
                new BigDecimal("2.5"), new BigDecimal("10.00"));
        ApuSnapshotEntity apuSnapshotEntity = new ApuSnapshotEntity();
        apuSnapshotEntity.setId(UUID.randomUUID());
        UUID createdBy = UUID.randomUUID();

        ApuInsumoSnapshotEntity entity = mapper.toEntity(domain, apuSnapshotEntity, createdBy);

        assertEquals(domain.getId().getValue(), entity.getId());
        assertEquals(domain.getRecursoExternalId(), entity.getRecursoExternalId());
        assertEquals(domain.getRecursoNombre(), entity.getRecursoNombre());
        assertEquals(domain.getCantidad(), entity.getCantidad());
        assertEquals(domain.getPrecioUnitario(), entity.getPrecioUnitario());
        assertEquals(domain.getSubtotal(), entity.getSubtotal());
        assertEquals(createdBy, entity.getCreatedBy());

        APUInsumoSnapshot roundtrip = mapper.toDomain(entity);
        assertEquals(domain.getId(), roundtrip.getId());
        assertEquals(domain.getRecursoExternalId(), roundtrip.getRecursoExternalId());
        assertEquals(domain.getRecursoNombre(), roundtrip.getRecursoNombre());
        assertEquals(domain.getCantidad(), roundtrip.getCantidad());
        assertEquals(domain.getPrecioUnitario(), roundtrip.getPrecioUnitario());
        assertEquals(domain.getSubtotal(), roundtrip.getSubtotal());
    }
}
