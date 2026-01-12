package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.logistica.inventario.InventarioItem;
import com.budgetpro.domain.logistica.inventario.InventarioId;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.mapper.InventarioMapper;
import com.budgetpro.infrastructure.persistence.repository.InventarioJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el puerto de salida InventarioRepository.
 * 
 * Implementa búsquedas con y sin filtro de proyectoId para soportar diferentes casos de uso.
 */
@Component
public class InventarioRepositoryAdapter implements InventarioRepository {

    private final InventarioJpaRepository jpaRepository;
    private final InventarioMapper mapper;
    private final RecursoJpaRepository recursoJpaRepository;

    public InventarioRepositoryAdapter(InventarioJpaRepository jpaRepository,
                                      InventarioMapper mapper,
                                      RecursoJpaRepository recursoJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.recursoJpaRepository = recursoJpaRepository;
    }

    @Override
    public void save(InventarioItem inventario) {
        // Este método solo funciona para actualizar inventarios existentes
        // Para crear nuevos, use save(InventarioItem, UUID proyectoId)
        if (inventario == null) {
            throw new IllegalArgumentException("El inventario no puede ser nulo");
        }

        Optional<InventarioItemEntity> existingEntityOpt = jpaRepository.findById(inventario.getId().getValue());

        if (existingEntityOpt.isEmpty()) {
            throw new IllegalStateException(
                "No se puede crear un nuevo InventarioItem sin proyectoId. " +
                "Use save(InventarioItem, UUID proyectoId) para crear nuevos inventarios."
            );
        }

        // Mapear dominio a entidad (Hibernate maneja optimistic locking automáticamente con @Version)
        InventarioItemEntity existingEntity = existingEntityOpt.get();
        InventarioItemEntity entityToSave = mapper.toEntity(inventario, existingEntity.getProyectoId(), 
                                          existingEntity.getRecurso(), existingEntity);

        jpaRepository.save(entityToSave);
    }
    
    @Override
    public void save(InventarioItem inventario, UUID proyectoId) {
        if (inventario == null) {
            throw new IllegalArgumentException("El inventario no puede ser nulo");
        }
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }

        Optional<InventarioItemEntity> existingEntityOpt = jpaRepository.findById(inventario.getId().getValue());

        InventarioItemEntity entityToSave;
        if (existingEntityOpt.isPresent()) {
            // Actualizar entidad existente
            InventarioItemEntity existingEntity = existingEntityOpt.get();
            
            // Verificar que el proyectoId coincida
            if (!existingEntity.getProyectoId().equals(proyectoId)) {
                throw new IllegalArgumentException(
                    String.format("El inventario %s pertenece al proyecto %s, no al proyecto %s",
                        inventario.getId(), existingEntity.getProyectoId(), proyectoId)
                );
            }
            
            // Mapear dominio a entidad (Hibernate maneja optimistic locking automáticamente con @Version)
            entityToSave = mapper.toEntity(inventario, proyectoId, existingEntity.getRecurso(), existingEntity);
        } else {
            // Crear nueva entidad
            RecursoEntity recursoEntity = recursoJpaRepository.findById(inventario.getRecursoId().getValue())
                    .orElseThrow(() -> new IllegalStateException(
                        String.format("No existe recurso con ID %s", inventario.getRecursoId())
                    ));
            
            entityToSave = mapper.toEntity(inventario, proyectoId, recursoEntity, null);
        }

        // Guardar (Hibernate maneja optimistic locking automáticamente con @Version)
        jpaRepository.save(entityToSave);
    }

    @Override
    public Optional<InventarioItem> findById(InventarioId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del inventario no puede ser nulo");
        }

        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<InventarioItem> findByRecursoId(RecursoId recursoId) {
        if (recursoId == null) {
            throw new IllegalArgumentException("El recursoId no puede ser nulo");
        }

        // Buscar el primer inventario con ese recursoId (puede haber múltiples por proyecto)
        // NOTA: Este método es ambiguo si hay múltiples proyectos. Se recomienda usar
        // un método con proyectoId cuando se conoce el proyectoId.
        // Por ahora, buscamos en todos los inventarios y retornamos el primero encontrado.
        return jpaRepository.findAll().stream()
                .filter(entity -> entity.getRecurso().getId().equals(recursoId.getValue()))
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public Map<RecursoId, InventarioItem> findAllByRecursoIds(List<RecursoId> recursoIds) {
        if (recursoIds == null || recursoIds.isEmpty()) {
            return new HashMap<>();
        }

        // Buscar inventarios por recursoIds (sin filtro de proyecto)
        // NOTA: Este método es ambiguo si hay múltiples proyectos con los mismos recursos.
        // Se recomienda usar findAllByProyectoIdAndRecursoIds() cuando se conoce el proyectoId.
        // Por ahora, tomamos el primero encontrado para cada recurso.
        Map<RecursoId, InventarioItem> result = new HashMap<>();
        
        List<InventarioItemEntity> allInventarios = jpaRepository.findAll();
        for (RecursoId recursoId : recursoIds) {
            allInventarios.stream()
                    .filter(entity -> entity.getRecurso().getId().equals(recursoId.getValue()))
                    .findFirst()
                    .map(mapper::toDomain)
                    .ifPresent(inventario -> result.put(recursoId, inventario));
        }

        return result;
    }
    
    @Override
    public Map<RecursoId, InventarioItem> findAllByProyectoIdAndRecursoIds(UUID proyectoId, List<RecursoId> recursoIds) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (recursoIds == null || recursoIds.isEmpty()) {
            return new HashMap<>();
        }

        // Convertir RecursoIds a UUIDs
        List<UUID> recursoUuids = recursoIds.stream()
                .map(RecursoId::getValue)
                .toList();

        // Buscar inventarios por proyectoId y recursoIds (búsqueda precisa)
        List<InventarioItemEntity> inventarios = jpaRepository.findByProyectoIdAndRecursoIds(proyectoId, recursoUuids);
        
        Map<RecursoId, InventarioItem> result = new HashMap<>();
        for (InventarioItemEntity entity : inventarios) {
            RecursoId recursoId = RecursoId.of(entity.getRecurso().getId());
            result.put(recursoId, mapper.toDomain(entity));
        }

        return result;
    }
}
