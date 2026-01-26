package com.budgetpro.infrastructure.persistence.adapter.requisicion;

import com.budgetpro.domain.logistica.requisicion.model.Requisicion;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;
import com.budgetpro.domain.logistica.requisicion.port.out.RequisicionRepository;
import com.budgetpro.infrastructure.persistence.entity.requisicion.RequisicionEntity;
import com.budgetpro.infrastructure.persistence.entity.requisicion.RequisicionItemEntity;
import com.budgetpro.infrastructure.persistence.mapper.requisicion.RequisicionMapper;
import com.budgetpro.infrastructure.persistence.repository.requisicion.RequisicionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para RequisicionRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class RequisicionRepositoryAdapter implements RequisicionRepository {

    private final RequisicionJpaRepository jpaRepository;
    private final RequisicionMapper mapper;

    public RequisicionRepositoryAdapter(RequisicionJpaRepository jpaRepository, RequisicionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Requisicion requisicion) {
        Optional<RequisicionEntity> existingEntityOpt = jpaRepository.findById(requisicion.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y sincronizar ítems
            RequisicionEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, requisicion);
            
            // Sincronizar ítems (actualizar cantidadDespachada)
            sincronizarItems(existingEntity, requisicion);
            
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            RequisicionEntity newEntity = mapper.toEntity(requisicion);
            jpaRepository.save(newEntity);
        }
    }

    /**
     * Sincroniza los ítems del dominio con los de la entidad.
     * Actualiza cantidadDespachada de ítems existentes.
     */
    private void sincronizarItems(RequisicionEntity existingEntity, Requisicion requisicion) {
        // Actualizar cantidadDespachada de ítems existentes
        for (com.budgetpro.domain.logistica.requisicion.model.RequisicionItem itemDomain : requisicion.getItems()) {
            RequisicionItemEntity itemEntity = existingEntity.getItems().stream()
                    .filter(item -> item.getId().equals(itemDomain.getId().getValue()))
                    .findFirst()
                    .orElse(null);
            
            if (itemEntity != null) {
                // Actualizar cantidadDespachada
                mapper.updateItemEntity(itemEntity, itemDomain);
            } else {
                // Nuevo ítem (raro, pero posible si se agregan ítems después de crear)
                RequisicionItemEntity newItemEntity = mapper.toItemEntity(itemDomain, existingEntity);
                existingEntity.getItems().add(newItemEntity);
            }
        }
        
        // Remover ítems que ya no están en el dominio (orphanRemoval maneja esto, pero por claridad)
        existingEntity.getItems().removeIf(itemEntity ->
                requisicion.getItems().stream()
                        .noneMatch(itemDomain -> itemDomain.getId().getValue().equals(itemEntity.getId()))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Requisicion> findById(RequisicionId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Requisicion> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
