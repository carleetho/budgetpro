package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.catalogo.model.EstadoProxy;
import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.infrastructure.persistence.entity.catalogo.RecursoProxyEntity;
import com.budgetpro.infrastructure.persistence.mapper.RecursoProxyMapper;
import com.budgetpro.infrastructure.persistence.repository.RecursoProxyJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para RecursoProxyRepository.
 */
@Component
public class RecursoProxyRepositoryAdapter implements RecursoProxyRepository {

    private final RecursoProxyJpaRepository jpaRepository;
    private final RecursoProxyMapper mapper;

    public RecursoProxyRepositoryAdapter(RecursoProxyJpaRepository jpaRepository,
                                         RecursoProxyMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecursoProxy> findById(UUID id) {
        return jpaRepository.findById(Objects.requireNonNull(id, "El ID no puede ser nulo"))
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecursoProxy> findByExternalId(String externalId, String catalogSource) {
        return jpaRepository.findByExternalIdAndCatalogSource(externalId, catalogSource)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public RecursoProxy save(RecursoProxy proxy) {
        Objects.requireNonNull(proxy, "El proxy no puede ser nulo");
        UUID proxyId = Objects.requireNonNull(proxy.getId().getValue(), "El ID del proxy no puede ser nulo");
        Optional<RecursoProxyEntity> existing = jpaRepository.findById(proxyId);
        if (existing.isPresent()) {
            RecursoProxyEntity entity = existing.get();
            entity.setExternalId(proxy.getExternalId());
            entity.setCatalogSource(proxy.getCatalogSource());
            entity.setNombreSnapshot(proxy.getNombreSnapshot());
            entity.setTipoSnapshot(proxy.getTipoSnapshot());
            entity.setUnidadSnapshot(proxy.getUnidadSnapshot());
            entity.setPrecioSnapshot(proxy.getPrecioSnapshot());
            entity.setSnapshotDate(proxy.getSnapshotDate());
            entity.setEstado(proxy.getEstado());
            RecursoProxyEntity saved = Objects.requireNonNull(jpaRepository.save(entity), "Entidad guardada no puede ser nula");
            return mapper.toDomain(saved);
        }

        UUID createdBy = getCurrentUserId();
        RecursoProxyEntity newEntity = Objects.requireNonNull(
                mapper.toEntity(proxy, createdBy),
                "Entidad no puede ser nula"
        );
        RecursoProxyEntity saved = Objects.requireNonNull(
                jpaRepository.save(newEntity),
                "Entidad guardada no puede ser nula"
        );
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecursoProxy> findObsoletos() {
        return jpaRepository.findByEstado(EstadoProxy.OBSOLETO).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private UUID getCurrentUserId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}
