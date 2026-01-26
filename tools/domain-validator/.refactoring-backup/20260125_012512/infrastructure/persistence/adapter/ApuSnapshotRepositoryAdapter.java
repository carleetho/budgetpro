package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuInsumoSnapshotEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import com.budgetpro.infrastructure.persistence.mapper.ApuSnapshotMapper;
import com.budgetpro.infrastructure.persistence.repository.ApuSnapshotJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para ApuSnapshotRepository.
 */
@Component
public class ApuSnapshotRepositoryAdapter implements ApuSnapshotRepository {

    private final ApuSnapshotJpaRepository jpaRepository;
    private final PartidaJpaRepository partidaJpaRepository;
    private final ApuSnapshotMapper mapper;

    public ApuSnapshotRepositoryAdapter(ApuSnapshotJpaRepository jpaRepository,
                                        PartidaJpaRepository partidaJpaRepository,
                                        ApuSnapshotMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.partidaJpaRepository = partidaJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<APUSnapshot> findById(UUID id) {
        return jpaRepository.findById(Objects.requireNonNull(id, "El ID no puede ser nulo"))
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<APUSnapshot> findByPartidaId(UUID partidaId) {
        return jpaRepository.findByPartidaId(Objects.requireNonNull(partidaId, "El partidaId no puede ser nulo"))
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public APUSnapshot save(APUSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "El snapshot no puede ser nulo");
        UUID snapshotId = Objects.requireNonNull(snapshot.getId().getValue(), "El ID del snapshot no puede ser nulo");
        Optional<ApuSnapshotEntity> existing = jpaRepository.findById(snapshotId);
        if (existing.isPresent()) {
            ApuSnapshotEntity entity = existing.get();
            entity.setExternalApuId(snapshot.getExternalApuId());
            entity.setCatalogSource(snapshot.getCatalogSource());
            entity.setRendimientoOriginal(snapshot.getRendimientoOriginal());
            entity.setRendimientoVigente(snapshot.getRendimientoVigente());
            entity.setRendimientoModificado(snapshot.isRendimientoModificado());
            entity.setRendimientoModificadoPor(snapshot.getRendimientoModificadoPor());
            entity.setRendimientoModificadoEn(snapshot.getRendimientoModificadoEn());
            entity.setUnidadSnapshot(snapshot.getUnidadSnapshot());
            entity.setSnapshotDate(snapshot.getSnapshotDate());

            entity.getInsumos().clear();
            List<ApuInsumoSnapshotEntity> insumos = mapper.toEntity(snapshot, entity.getPartida(), getCurrentUserId())
                    .getInsumos();
            entity.getInsumos().addAll(insumos);

            ApuSnapshotEntity saved = Objects.requireNonNull(jpaRepository.save(entity), "Entidad guardada no puede ser nula");
            return mapper.toDomain(saved);
        }

        PartidaEntity partidaEntity = partidaJpaRepository.findById(
                        Objects.requireNonNull(snapshot.getPartidaId(), "El partidaId no puede ser nulo")
                )
                .orElseThrow(() -> new IllegalStateException("Partida no encontrada: " + snapshot.getPartidaId()));
        ApuSnapshotEntity newEntity = Objects.requireNonNull(
                mapper.toEntity(snapshot, partidaEntity, getCurrentUserId()),
                "Entidad no puede ser nula"
        );
        ApuSnapshotEntity saved = Objects.requireNonNull(
                jpaRepository.save(newEntity),
                "Entidad guardada no puede ser nula"
        );
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<APUSnapshot> findModificados() {
        return jpaRepository.findByRendimientoModificadoTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private UUID getCurrentUserId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
