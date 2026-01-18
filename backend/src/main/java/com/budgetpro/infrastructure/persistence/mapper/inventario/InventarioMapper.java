package com.budgetpro.infrastructure.persistence.mapper.inventario;

import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventarioId;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.inventario.InventarioItemEntity;
import com.budgetpro.infrastructure.persistence.entity.inventario.MovimientoInventarioEntity;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre InventarioItem (dominio) y InventarioItemEntity (persistencia).
 */
@Component
public class InventarioMapper {

    private final RecursoJpaRepository recursoJpaRepository;

    public InventarioMapper(RecursoJpaRepository recursoJpaRepository) {
        this.recursoJpaRepository = recursoJpaRepository;
    }

    /**
     * Convierte un InventarioItem (dominio) a InventarioItemEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public InventarioItemEntity toEntity(InventarioItem item) {
        if (item == null) {
            return null;
        }

        // Cargar el recurso
        RecursoEntity recursoEntity = recursoJpaRepository.findById(item.getRecursoId())
                .orElseThrow(() -> new IllegalStateException("Recurso no encontrado: " + item.getRecursoId()));

        InventarioItemEntity entity = new InventarioItemEntity(
            item.getId().getValue(),
            item.getProyectoId(),
            recursoEntity,
            item.getCantidadFisica(),
            item.getCostoPromedio(),
            item.getUbicacion(),
            item.getUltimaActualizacion(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );

        // Mapear movimientos nuevos
        List<MovimientoInventarioEntity> movimientosEntities = item.getMovimientosNuevos().stream()
                .map(movimiento -> toMovimientoEntity(movimiento, entity))
                .collect(Collectors.toList());
        entity.setMovimientos(movimientosEntities);

        return entity;
    }

    /**
     * Convierte un MovimientoInventario (dominio) a MovimientoInventarioEntity (persistencia).
     */
    public MovimientoInventarioEntity toMovimientoEntity(MovimientoInventario movimiento, InventarioItemEntity itemEntity) {
        if (movimiento == null) {
            return null;
        }

        return new MovimientoInventarioEntity(
            movimiento.getId().getValue(),
            itemEntity,
            movimiento.getTipo(),
            movimiento.getCantidad(),
            movimiento.getCostoUnitario(),
            movimiento.getCostoTotal(),
            movimiento.getCompraDetalleId(),
            movimiento.getReferencia(),
            movimiento.getFechaHora(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un InventarioItemEntity (persistencia) a InventarioItem (dominio).
     */
    public InventarioItem toDomain(InventarioItemEntity entity) {
        if (entity == null) {
            return null;
        }

        UUID recursoId = entity.getRecurso() != null ? entity.getRecurso().getId() : null;

        return InventarioItem.reconstruir(
            InventarioId.of(entity.getId()),
            entity.getProyectoId(),
            recursoId,
            entity.getCantidadFisica(),
            entity.getCostoPromedio(),
            entity.getUbicacion(),
            entity.getUltimaActualizacion(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Convierte un MovimientoInventarioEntity (persistencia) a MovimientoInventario (dominio).
     */
    public MovimientoInventario toMovimientoDomain(MovimientoInventarioEntity entity) {
        if (entity == null) {
            return null;
        }

        UUID inventarioItemId = entity.getInventarioItem() != null ? entity.getInventarioItem().getId() : null;

        return MovimientoInventario.reconstruir(
            MovimientoInventarioId.of(entity.getId()),
            inventarioItemId,
            entity.getTipo(),
            entity.getCantidad(),
            entity.getCostoUnitario(),
            entity.getCostoTotal(),
            entity.getCompraDetalleId(),
            entity.getReferencia(),
            entity.getFechaHora()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(InventarioItemEntity existingEntity, InventarioItem item) {
        existingEntity.setCantidadFisica(item.getCantidadFisica());
        existingEntity.setCostoPromedio(item.getCostoPromedio());
        existingEntity.setUbicacion(item.getUbicacion());
        existingEntity.setUltimaActualizacion(item.getUltimaActualizacion());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca proyectoId ni recursoId (son inmutables)
        
        // Sincronizar movimientos nuevos
        sincronizarMovimientos(existingEntity, item);
    }

    /**
     * Sincroniza los movimientos nuevos del dominio con los de la entidad.
     */
    private void sincronizarMovimientos(InventarioItemEntity existingEntity, InventarioItem item) {
        // Limpiar movimientos existentes y agregar los nuevos
        existingEntity.getMovimientos().clear();
        
        // Agregar movimientos nuevos
        for (MovimientoInventario movimiento : item.getMovimientosNuevos()) {
            MovimientoInventarioEntity movimientoEntity = toMovimientoEntity(movimiento, existingEntity);
            existingEntity.getMovimientos().add(movimientoEntity);
        }
    }
}
