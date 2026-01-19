package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.port.out.CompraRepository;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraDetalleEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import com.budgetpro.infrastructure.persistence.mapper.compra.CompraMapper;
import com.budgetpro.infrastructure.persistence.repository.compra.CompraJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para CompraRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class CompraRepositoryAdapter implements CompraRepository {

    private final CompraJpaRepository jpaRepository;
    private final CompraMapper mapper;

    public CompraRepositoryAdapter(CompraJpaRepository jpaRepository, CompraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Compra compra) {
        Optional<CompraEntity> existingEntityOpt = jpaRepository.findById(compra.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            CompraEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, compra);
            
            // Actualizar detalles (cascade y orphanRemoval manejan la sincronización)
            sincronizarDetalles(existingEntity, compra);
            
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear (ya no se necesita cargar recursos, usamos referencias externas)
            CompraEntity newEntity = mapper.toEntity(compra);
            jpaRepository.save(newEntity);
        }
    }

    /**
     * Sincroniza los detalles del dominio con los de la entidad.
     */
    private void sincronizarDetalles(CompraEntity existingEntity, Compra compra) {
        // Limpiar detalles existentes y agregar los nuevos
        existingEntity.getDetalles().clear();
        
        // Crear nuevos detalles (ya no se necesita cargar RecursoEntity, usamos referencias externas)
        for (com.budgetpro.domain.logistica.compra.model.CompraDetalle detalleDomain : compra.getDetalles()) {
            CompraDetalleEntity detalleEntity = mapper.toDetalleEntity(detalleDomain, existingEntity);
            existingEntity.getDetalles().add(detalleEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Compra> findById(CompraId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
