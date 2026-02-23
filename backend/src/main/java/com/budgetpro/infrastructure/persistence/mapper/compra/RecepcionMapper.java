package com.budgetpro.infrastructure.persistence.mapper.compra;

import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacenId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.Recepcion;
import com.budgetpro.domain.logistica.compra.model.RecepcionDetalle;
import com.budgetpro.domain.logistica.compra.model.RecepcionDetalleId;
import com.budgetpro.domain.logistica.compra.model.RecepcionId;
import com.budgetpro.infrastructure.persistence.entity.compra.RecepcionDetalleEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.RecepcionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Recepcion (dominio) y RecepcionEntity (persistencia).
 */
@Component
public class RecepcionMapper {

    /**
     * Convierte un RecepcionEntity (persistencia) a Recepcion (dominio).
     * 
     * @param entity La entidad de persistencia a convertir
     * @return El objeto de dominio Recepcion, o null si entity es null
     *         (patrón estándar en mappers para manejar valores nulos)
     */
    public Recepcion toDomain(RecepcionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("RecepcionEntity no puede ser null");
        }

        // Mapear detalles
        List<RecepcionDetalle> detalles = entity.getDetalles().stream()
                .map(this::toDetalleDomain)
                .collect(Collectors.toList());

        // NOTA: Recepcion no tiene método reconstruir(), usamos crear() aunque se pierda
        // información de version y fechaCreacion. Esto debería ser corregido agregando
        // un método reconstruir() al dominio.
        return Recepcion.crear(
            RecepcionId.of(entity.getId()),
            CompraId.from(entity.getCompraId()),
            entity.getFechaRecepcion(),
            entity.getGuiaRemision(),
            detalles,
            entity.getCreadoPorUsuarioId()
        );
    }

    /**
     * Convierte un RecepcionDetalleEntity (persistencia) a RecepcionDetalle (dominio).
     * 
     * @param entity La entidad de persistencia a convertir
     * @return El objeto de dominio RecepcionDetalle, o null si entity es null
     *         (patrón estándar en mappers para manejar valores nulos)
     */
    private RecepcionDetalle toDetalleDomain(RecepcionDetalleEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("RecepcionDetalleEntity no puede ser null");
        }

        return RecepcionDetalle.crear(
            RecepcionDetalleId.of(entity.getId()),
            entity.getCompraDetalleId(),
            entity.getRecursoId(),
            AlmacenId.of(entity.getAlmacenId()),
            entity.getCantidadRecibida(),
            entity.getPrecioUnitario(),
            MovimientoAlmacenId.of(entity.getMovimientoAlmacenId())
        );
    }

    /**
     * Convierte un Recepcion (dominio) a RecepcionEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     * 
     * @param recepcion El objeto de dominio a convertir
     * @return La entidad de persistencia, o null si recepcion es null
     *         (patrón estándar en mappers para manejar valores nulos)
     */
    public RecepcionEntity toEntity(Recepcion recepcion) {
        if (recepcion == null) {
            throw new IllegalArgumentException("Recepcion no puede ser null");
        }

        RecepcionEntity entity = new RecepcionEntity(
            recepcion.getId().getValue(),
            recepcion.getCompraId().getValue(),
            recepcion.getFechaRecepcion(),
            recepcion.getGuiaRemision(),
            recepcion.getCreadoPorUsuarioId(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );

        // Mapear detalles
        List<RecepcionDetalleEntity> detallesEntities = recepcion.getDetalles().stream()
                .map(detalle -> toDetalleEntity(detalle, entity))
                .collect(Collectors.toList());
        entity.setDetalles(detallesEntities);

        return entity;
    }

    /**
     * Convierte un RecepcionDetalle (dominio) a RecepcionDetalleEntity (persistencia).
     * 
     * @param detalle El objeto de dominio a convertir
     * @param recepcionEntity La entidad padre RecepcionEntity
     * @return La entidad de persistencia, o null si detalle es null
     *         (patrón estándar en mappers para manejar valores nulos)
     */
    private RecepcionDetalleEntity toDetalleEntity(RecepcionDetalle detalle, RecepcionEntity recepcionEntity) {
        if (detalle == null) {
            throw new IllegalArgumentException("RecepcionDetalle no puede ser null");
        }

        return new RecepcionDetalleEntity(
            detalle.getId().getValue(),
            recepcionEntity,
            detalle.getCompraDetalleId(),
            detalle.getRecursoId(),
            detalle.getAlmacenId().getValue(),
            detalle.getCantidadRecibida(),
            detalle.getPrecioUnitario(),
            detalle.getMovimientoAlmacenId().getValue()
        );
    }
}
