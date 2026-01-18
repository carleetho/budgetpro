package com.budgetpro.infrastructure.persistence.mapper.almacen;

import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacenId;
import com.budgetpro.infrastructure.persistence.entity.almacen.MovimientoAlmacenEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre MovimientoAlmacen (dominio) y MovimientoAlmacenEntity (persistencia).
 */
@Component
public class MovimientoAlmacenMapper {

    /**
     * Convierte un MovimientoAlmacen (dominio) a MovimientoAlmacenEntity (persistencia) para CREACIÓN.
     */
    public MovimientoAlmacenEntity toEntity(MovimientoAlmacen movimiento) {
        if (movimiento == null) {
            return null;
        }

        return new MovimientoAlmacenEntity(
            movimiento.getId().getValue(),
            movimiento.getAlmacenId().getValue(),
            movimiento.getRecursoId(),
            movimiento.getTipoMovimiento(),
            movimiento.getFechaMovimiento(),
            movimiento.getCantidad(),
            movimiento.getPrecioUnitario(),
            movimiento.getImporteTotal(),
            movimiento.getNumeroDocumento(),
            movimiento.getPartidaId(),
            movimiento.getCentroCostoId(),
            movimiento.getObservaciones(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un MovimientoAlmacenEntity (persistencia) a MovimientoAlmacen (dominio).
     */
    public MovimientoAlmacen toDomain(MovimientoAlmacenEntity entity) {
        if (entity == null) {
            return null;
        }

        return MovimientoAlmacen.reconstruir(
            MovimientoAlmacenId.of(entity.getId()),
            com.budgetpro.domain.logistica.almacen.model.AlmacenId.of(entity.getAlmacenId()),
            entity.getRecursoId(),
            entity.getTipoMovimiento(),
            entity.getFechaMovimiento(),
            entity.getCantidad(),
            entity.getPrecioUnitario(),
            entity.getImporteTotal(),
            entity.getNumeroDocumento(),
            entity.getPartidaId(),
            entity.getCentroCostoId(),
            entity.getObservaciones()
        );
    }
}
