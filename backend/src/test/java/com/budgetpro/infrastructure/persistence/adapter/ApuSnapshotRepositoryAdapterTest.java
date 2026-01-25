package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import com.budgetpro.infrastructure.persistence.mapper.ApuInsumoSnapshotMapper;
import com.budgetpro.infrastructure.persistence.mapper.ApuSnapshotMapper;
import com.budgetpro.infrastructure.persistence.mapper.ComposicionCuadrillaSnapshotMapper;
import com.budgetpro.infrastructure.persistence.repository.ApuSnapshotJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@SuppressWarnings({ "NullAway", "null" })
@ExtendWith(MockitoExtension.class)
class ApuSnapshotRepositoryAdapterTest {

    @Mock
    private ApuSnapshotJpaRepository jpaRepository;

    @Mock
    private PartidaJpaRepository partidaJpaRepository;

    private final ApuSnapshotMapper mapper = new ApuSnapshotMapper(
            new ApuInsumoSnapshotMapper(new ComposicionCuadrillaSnapshotMapper()));

    @SuppressWarnings({ "NullAway", "Nullness" })
    @Test
    void save_nuevoDebePersistir() {
        APUSnapshot snapshot = APUSnapshot.crear(APUSnapshotId.generate(), UUID.randomUUID(), "APU-EXT-1", "CAT-A",
                new BigDecimal("2.0"), "UND", LocalDateTime.now());

        PartidaEntity partidaEntity = new PartidaEntity();
        partidaEntity.setId(Objects.requireNonNull(snapshot.getPartidaId()));

        UUID snapshotId = Objects.requireNonNull(snapshot.getId().getValue());
        when(jpaRepository.findById(snapshotId)).thenReturn(Optional.empty());
        when(partidaJpaRepository.findById(Objects.requireNonNull(snapshot.getPartidaId())))
                .thenReturn(Optional.of(partidaEntity));
        when(jpaRepository.save(org.mockito.ArgumentMatchers.<ApuSnapshotEntity>notNull()))
                .thenAnswer(invocation -> Objects.requireNonNull(invocation.getArgument(0)));

        ApuSnapshotRepositoryAdapter adapter = new ApuSnapshotRepositoryAdapter(jpaRepository, partidaJpaRepository,
                mapper);
        APUSnapshot saved = adapter.save(snapshot);

        assertEquals(snapshot.getExternalApuId(), saved.getExternalApuId());
    }

    @SuppressWarnings({ "NullAway", "Nullness" })
    @Test
    void findByPartidaId_debeMapear() {
        ApuSnapshotEntity entity = new ApuSnapshotEntity();
        entity.setId(Objects.requireNonNull(UUID.randomUUID()));
        PartidaEntity partida = new PartidaEntity();
        partida.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        entity.setPartida(partida);
        entity.setExternalApuId("APU-EXT-2");
        entity.setCatalogSource("CAT-B");
        entity.setRendimientoOriginal(new BigDecimal("1.0"));
        entity.setRendimientoVigente(new BigDecimal("1.0"));
        entity.setUnidadSnapshot("UND");
        entity.setSnapshotDate(LocalDateTime.now());
        entity.setInsumos(List.of());

        when(jpaRepository.findByPartidaId(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.of(entity));

        ApuSnapshotRepositoryAdapter adapter = new ApuSnapshotRepositoryAdapter(jpaRepository, partidaJpaRepository,
                mapper);
        Optional<APUSnapshot> found = adapter.findByPartidaId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertTrue(found.isPresent());
        assertEquals("APU-EXT-2", found.get().getExternalApuId());
    }

    @SuppressWarnings({ "NullAway", "Nullness" })
    @Test
    void findModificados_debeMapearLista() {
        ApuSnapshotEntity entity = new ApuSnapshotEntity();
        entity.setId(Objects.requireNonNull(UUID.randomUUID()));
        PartidaEntity partida = new PartidaEntity();
        partida.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        entity.setPartida(partida);
        entity.setExternalApuId("APU-EXT-3");
        entity.setCatalogSource("CAT-C");
        entity.setRendimientoOriginal(new BigDecimal("1.0"));
        entity.setRendimientoVigente(new BigDecimal("1.5"));
        entity.setRendimientoModificado(true);
        entity.setUnidadSnapshot("UND");
        entity.setSnapshotDate(LocalDateTime.now());
        entity.setInsumos(List.of());

        when(jpaRepository.findByRendimientoModificadoTrue()).thenReturn(List.of(entity));

        ApuSnapshotRepositoryAdapter adapter = new ApuSnapshotRepositoryAdapter(jpaRepository, partidaJpaRepository,
                mapper);
        List<APUSnapshot> results = adapter.findModificados();

        assertEquals(1, results.size());
        assertEquals("APU-EXT-3", results.get(0).getExternalApuId());
    }
}
