package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.catalogo.RecursoProxyEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursoProxyMapperTest {

    private final RecursoProxyMapper mapper = new RecursoProxyMapper();

    @Test
    void toEntity_y_toDomain_debenPreservarDatos() {
        RecursoProxy domain = RecursoProxy.crear(
                RecursoProxyId.generate(),
                "EXT-001",
                "CATALOGO-A",
                "Cemento Gris",
                TipoRecurso.MATERIAL,
                "BOLSA",
                new BigDecimal("12.50"),
                LocalDateTime.now()
        );
        UUID createdBy = UUID.randomUUID();

        RecursoProxyEntity entity = mapper.toEntity(domain, createdBy);

        assertEquals(domain.getId().getValue(), entity.getId());
        assertEquals(domain.getExternalId(), entity.getExternalId());
        assertEquals(domain.getCatalogSource(), entity.getCatalogSource());
        assertEquals(domain.getNombreSnapshot(), entity.getNombreSnapshot());
        assertEquals(domain.getTipoSnapshot(), entity.getTipoSnapshot());
        assertEquals(domain.getUnidadSnapshot(), entity.getUnidadSnapshot());
        assertEquals(domain.getPrecioSnapshot(), entity.getPrecioSnapshot());
        assertEquals(domain.getSnapshotDate(), entity.getSnapshotDate());
        assertEquals(createdBy, entity.getCreatedBy());

        RecursoProxy roundtrip = mapper.toDomain(entity);
        assertEquals(domain.getId(), roundtrip.getId());
        assertEquals(domain.getExternalId(), roundtrip.getExternalId());
        assertEquals(domain.getCatalogSource(), roundtrip.getCatalogSource());
        assertEquals(domain.getNombreSnapshot(), roundtrip.getNombreSnapshot());
        assertEquals(domain.getTipoSnapshot(), roundtrip.getTipoSnapshot());
        assertEquals(domain.getUnidadSnapshot(), roundtrip.getUnidadSnapshot());
        assertEquals(domain.getPrecioSnapshot(), roundtrip.getPrecioSnapshot());
    }
}
