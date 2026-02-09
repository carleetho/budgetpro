package com.budgetpro.infrastructure.persistence.adapter.inventario;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.infrastructure.persistence.entity.inventario.InventarioItemEntity;
import com.budgetpro.infrastructure.persistence.mapper.inventario.InventarioMapper;
import com.budgetpro.infrastructure.persistence.repository.bodega.BodegaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.inventario.InventarioItemJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para InventarioRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión. Hibernate maneja el
 * Optimistic Locking automáticamente con @Version.
 */
@Component
public class InventarioRepositoryAdapter implements InventarioRepository {

    private final InventarioItemJpaRepository jpaRepository;
    private final BodegaJpaRepository bodegaJpaRepository;
    private final InventarioMapper mapper;

    public InventarioRepositoryAdapter(InventarioItemJpaRepository jpaRepository,
            BodegaJpaRepository bodegaJpaRepository, InventarioMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.bodegaJpaRepository = bodegaJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(InventarioItem item) {
        Optional<InventarioItemEntity> existingEntityOpt = jpaRepository.findById(item.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            InventarioItemEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, item);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: cargar BodegaEntity, mapear y guardar
            var bodegaEntity = bodegaJpaRepository.findById(item.getBodegaId().getValue()).orElseThrow(
                    () -> new IllegalArgumentException("Bodega no encontrada: " + item.getBodegaId().getValue()));
            InventarioItemEntity newEntity = mapper.toEntity(item, bodegaEntity);
            jpaRepository.save(newEntity);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventarioItem> findById(InventarioId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventarioItem> findByProyectoIdAndRecursoId(UUID proyectoId, UUID recursoId) {
        return jpaRepository.findByProyectoIdAndRecursoId(proyectoId, recursoId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioItem> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventarioItem> findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(UUID proyectoId,
            String recursoExternalId, String unidadBase, BodegaId bodegaId) {
        if (bodegaId == null) {
            throw new IllegalArgumentException("BodegaId cannot be null");
        }
        return jpaRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodega_Id(proyectoId,
                recursoExternalId, unidadBase, bodegaId.getValue()).map(mapper::toDomain);
    }
}
