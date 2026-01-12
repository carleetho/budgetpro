package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.logistica.inventario.InventarioItem;
import com.budgetpro.domain.logistica.inventario.InventarioId;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.infrastructure.persistence.entity.InventarioItemEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapper para convertir entre InventarioItemEntity (capa de infraestructura)
 * y InventarioItem (capa de dominio).
 */
@Component
public class InventarioMapper {

    /**
     * Convierte una InventarioItemEntity a un InventarioItem del dominio.
     */
    public InventarioItem toDomain(InventarioItemEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        InventarioId id = InventarioId.of(entity.getId());
        RecursoId recursoId = RecursoId.of(entity.getRecurso().getId());
        BigDecimal stock = entity.getCantidad(); // cantidad (ERD) -> stock (dominio)
        Long version = entity.getVersion() != null ? entity.getVersion().longValue() : 0L;

        return InventarioItem.reconstruir(id, recursoId, stock, version);
    }

    /**
     * Convierte un InventarioItem del dominio a una InventarioItemEntity.
     */
    public InventarioItemEntity toEntity(InventarioItem inventario, UUID proyectoId, 
                                        RecursoEntity recursoEntity, InventarioItemEntity existingEntity) {
        if (inventario == null) {
            throw new IllegalArgumentException("El inventario no puede ser nulo");
        }
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (recursoEntity == null) {
            throw new IllegalArgumentException("La entidad Recurso no puede ser nula");
        }

        InventarioItemEntity entity;
        if (existingEntity != null) {
            // Actualizar entidad existente
            entity = existingEntity;
            entity.setCantidad(inventario.getStock()); // stock (dominio) -> cantidad (ERD)

        } else {
            // Crear nueva entidad
            entity = new InventarioItemEntity(
                inventario.getId().getValue(),
                proyectoId,
                recursoEntity,
                inventario.getStock(), // stock (dominio) -> cantidad (ERD)
                BigDecimal.ZERO, // costo_promedio inicial en 0
                null // version inicial
            );
        }

        return entity;
    }
}
