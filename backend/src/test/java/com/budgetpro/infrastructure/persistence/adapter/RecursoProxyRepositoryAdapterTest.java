package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.catalogo.RecursoProxyEntity;
import com.budgetpro.infrastructure.persistence.mapper.RecursoProxyMapper;
import com.budgetpro.infrastructure.persistence.repository.RecursoProxyJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"NullAway", "null"})
@ExtendWith(MockitoExtension.class)
class RecursoProxyRepositoryAdapterTest {

    @Mock
    private RecursoProxyJpaRepository jpaRepository;

    private final RecursoProxyMapper mapper = new RecursoProxyMapper();

    @SuppressWarnings({"NullAway", "Nullness"})
    @Test
    void save_nuevoDebePersistir() {
        RecursoProxy proxy = RecursoProxy.crear(
                RecursoProxyId.generate(),
                "EXT-1",
                "CAT-A",
                "Arena fina",
                TipoRecurso.MATERIAL,
                "M3",
                new BigDecimal("25.00"),
                LocalDateTime.now()
        );

        UUID proxyId = Objects.requireNonNull(proxy.getId().getValue());
        when(jpaRepository.findById(proxyId)).thenReturn(Optional.empty());
        when(jpaRepository.save(org.mockito.ArgumentMatchers.<RecursoProxyEntity>notNull()))
                .thenAnswer(invocation -> Objects.requireNonNull(invocation.getArgument(0)));

        RecursoProxyRepositoryAdapter adapter = new RecursoProxyRepositoryAdapter(jpaRepository, mapper);
        RecursoProxy saved = adapter.save(proxy);

        ArgumentCaptor<RecursoProxyEntity> captor = ArgumentCaptor.forClass(RecursoProxyEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertEquals(proxy.getExternalId(), Objects.requireNonNull(captor.getValue()).getExternalId());
        assertEquals(proxy.getCatalogSource(), saved.getCatalogSource());
    }

    @SuppressWarnings({"NullAway", "Nullness"})
    @Test
    void findByExternalId_debeMapear() {
        RecursoProxyEntity entity = new RecursoProxyEntity();
        entity.setId(Objects.requireNonNull(UUID.randomUUID()));
        entity.setExternalId("EXT-2");
        entity.setCatalogSource("CAT-B");
        entity.setNombreSnapshot("Cemento");
        entity.setTipoSnapshot(TipoRecurso.MATERIAL);
        entity.setUnidadSnapshot("BOLSA");
        entity.setPrecioSnapshot(new BigDecimal("12.00"));
        entity.setSnapshotDate(LocalDateTime.now());
        entity.setEstado(com.budgetpro.domain.catalogo.model.EstadoProxy.ACTIVO);

        when(jpaRepository.findByExternalIdAndCatalogSource("EXT-2", "CAT-B"))
                .thenReturn(Optional.of(entity));

        RecursoProxyRepositoryAdapter adapter = new RecursoProxyRepositoryAdapter(jpaRepository, mapper);
        Optional<RecursoProxy> found = adapter.findByExternalId("EXT-2", "CAT-B");

        assertTrue(found.isPresent());
        assertEquals("EXT-2", found.get().getExternalId());
    }

    @SuppressWarnings({"NullAway", "Nullness"})
    @Test
    void findObsoletos_debeMapearLista() {
        RecursoProxyEntity entity = new RecursoProxyEntity();
        entity.setId(Objects.requireNonNull(UUID.randomUUID()));
        entity.setExternalId("EXT-3");
        entity.setCatalogSource("CAT-C");
        entity.setNombreSnapshot("Grava");
        entity.setTipoSnapshot(TipoRecurso.MATERIAL);
        entity.setUnidadSnapshot("M3");
        entity.setPrecioSnapshot(new BigDecimal("30.00"));
        entity.setSnapshotDate(LocalDateTime.now());
        entity.setEstado(com.budgetpro.domain.catalogo.model.EstadoProxy.OBSOLETO);

        when(jpaRepository.findByEstado(com.budgetpro.domain.catalogo.model.EstadoProxy.OBSOLETO))
                .thenReturn(List.of(entity));

        RecursoProxyRepositoryAdapter adapter = new RecursoProxyRepositoryAdapter(jpaRepository, mapper);
        List<RecursoProxy> results = adapter.findObsoletos();

        assertEquals(1, results.size());
        assertEquals("EXT-3", results.get(0).getExternalId());
    }
}
