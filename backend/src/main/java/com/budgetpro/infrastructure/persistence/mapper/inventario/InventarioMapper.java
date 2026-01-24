package com.budgetpro.infrastructure.persistence.mapper.inventario;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventarioId;
import com.budgetpro.infrastructure.persistence.entity.BodegaEntity;
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
    // TODO: Inyectar BodegaJpaRepository cuando esté disponible (Task 3)
    // Por ahora, se requiere pasar BodegaEntity explícitamente o cargarla externamente

    public InventarioMapper(RecursoJpaRepository recursoJpaRepository) {
        this.recursoJpaRepository = recursoJpaRepository;
    }

    /**
     * Convierte un InventarioItem (dominio) a InventarioItemEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     * 
     * @param item Item de inventario del dominio
     * @param bodegaEntity Entidad BodegaEntity (debe ser cargada previamente)
     */
    public InventarioItemEntity toEntity(InventarioItem item, BodegaEntity bodegaEntity) {
        if (item == null) {
            return null;
        }
        if (bodegaEntity == null) {
            throw new IllegalArgumentException("BodegaEntity es obligatoria para crear InventarioItemEntity");
        }

        // Cargar el recurso (puede ser null durante migración)
        RecursoEntity recursoEntity = null;
        if (item.getRecursoId() != null) {
            recursoEntity = recursoJpaRepository.findById(item.getRecursoId())
                    .orElse(null); // No lanzar excepción durante migración
        }

        InventarioItemEntity entity = new InventarioItemEntity(
            item.getId().getValue(),
            item.getProyectoId(),
            recursoEntity, // Puede ser null
            item.getRecursoExternalId(),
            bodegaEntity,
            item.getNombre(),
            item.getClasificacion(),
            item.getUnidadBase(),
            item.getCantidadFisica(),
            item.getCostoPromedio(),
            item.getUbicacion(), // DEPRECATED
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
     * @deprecated Usar toEntity(InventarioItem, BodegaEntity) en su lugar.
     * Este método se mantiene para compatibilidad durante migración.
     */
    @Deprecated
    public InventarioItemEntity toEntity(InventarioItem item) {
        throw new UnsupportedOperationException(
            "Usar toEntity(InventarioItem, BodegaEntity) con bodegaEntity. " +
            "Este método está deprecado y no debe usarse en nuevo código."
        );
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
        BodegaId bodegaId = entity.getBodega() != null 
                ? BodegaId.of(entity.getBodega().getId()) 
                : null;

        if (bodegaId == null) {
            throw new IllegalStateException("BodegaEntity es obligatoria en InventarioItemEntity");
        }

        return InventarioItem.reconstruir(
            InventarioId.of(entity.getId()),
            entity.getProyectoId(),
            recursoId, // Puede ser null durante migración
            entity.getRecursoExternalId(),
            bodegaId,
            entity.getNombre(),
            entity.getClasificacion(),
            entity.getUnidadBase(),
            entity.getCantidadFisica(),
            entity.getCostoPromedio(),
            entity.getUbicacion(), // DEPRECATED
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
     * CRÍTICO: Los campos snapshot (nombre, clasificacion, unidadBase, recursoExternalId, bodegaId) son inmutables.
     */
    public void updateEntity(InventarioItemEntity existingEntity, InventarioItem item) {
        existingEntity.setCantidadFisica(item.getCantidadFisica());
        existingEntity.setCostoPromedio(item.getCostoPromedio());
        existingEntity.setUbicacion(item.getUbicacion()); // DEPRECATED
        existingEntity.setUltimaActualizacion(item.getUltimaActualizacion());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se tocan campos inmutables: proyectoId, recursoExternalId, nombre, clasificacion, unidadBase, bodegaId
        
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
