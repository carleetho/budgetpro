package com.budgetpro.infrastructure.persistence.mapper.compra;

import com.budgetpro.domain.finanzas.compra.*;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraDetalleEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper que realiza la conversión entre el agregado de dominio Compra
 * y la entidad JPA CompraEntity.
 * 
 * Responsabilidades:
 * - Convertir Compra (dominio) → CompraEntity (JPA)
 * - Convertir CompraEntity (JPA) → Compra (dominio)
 * - Mapear detalles internos (DetalleCompra ↔ CompraDetalleEntity)
 * 
 * REGLA: Este mapper NO contiene lógica de negocio.
 * Solo realiza conversiones de datos entre capas.
 */
@Component
public class CompraMapper {

    /**
     * Convierte un agregado de dominio Compra a una entidad JPA CompraEntity.
     * 
     * @param compra El agregado de dominio (no puede ser nulo)
     * @return La entidad JPA correspondiente
     */
    public CompraEntity toEntity(Compra compra) {
        if (compra == null) {
            throw new IllegalArgumentException("La compra no puede ser nula");
        }

        // Para nuevas entidades, pasamos null para que Hibernate inicialice la versión
        // Si la compra ya tiene versión (reconstrucción), la pasamos
        Integer versionEntity = compra.getVersion() != null ? compra.getVersion().intValue() : null;
        CompraEntity entity = new CompraEntity(
            compra.getId().getValue(),
            compra.getProyectoId(),
            compra.getPresupuestoId(),
            compra.getEstado().name(),
            compra.getTotalAsMonto().getValue(),
            versionEntity
        );

        // Mapear detalles
        List<CompraDetalleEntity> detallesEntities = compra.getDetalles().stream()
                .map(detalle -> toDetalleEntity(detalle, entity))
                .collect(Collectors.toList());
        entity.setDetalles(detallesEntities);

        return entity;
    }

    /**
     * Convierte una entidad JPA CompraEntity a un agregado de dominio Compra.
     * 
     * @param entity La entidad JPA (no puede ser nula)
     * @return El agregado de dominio correspondiente
     */
    public Compra toDomain(CompraEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        // Convertir detalles
        List<DetalleCompra> detalles = entity.getDetalles().stream()
                .map(this::toDetalleDomain)
                .collect(Collectors.toList());

        // Convertir estado
        EstadoCompra estado = EstadoCompra.valueOf(entity.getEstado());

        // Convertir total
        TotalCompra total = TotalCompra.of(entity.getTotal());

        // Reconstruir agregado de dominio
        return Compra.reconstruir(
            CompraId.of(entity.getId()),
            entity.getProyectoId(),
            entity.getPresupuestoId(),
            detalles,
            estado,
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L,
            total
        );
    }

    /**
     * Convierte un DetalleCompra (dominio) a una CompraDetalleEntity (JPA).
     * 
     * @param detalle El detalle de dominio (no puede ser nulo)
     * @param compraEntity La entidad compra padre (no puede ser nula)
     * @return La entidad JPA correspondiente
     */
    private CompraDetalleEntity toDetalleEntity(DetalleCompra detalle, CompraEntity compraEntity) {
        // Generar ID para el detalle (los detalles no tienen ID en el dominio, se generan aquí)
        java.util.UUID detalleId = java.util.UUID.randomUUID();
        
        return new CompraDetalleEntity(
            detalleId,
            compraEntity,
            detalle.getRecursoId().getValue(),
            detalle.getCantidad().getValue(),
            detalle.getPrecioUnitario().getValue().getValue()
        );
    }

    /**
     * Convierte una CompraDetalleEntity (JPA) a un DetalleCompra (dominio).
     * 
     * @param entity La entidad JPA (no puede ser nula)
     * @return El detalle de dominio correspondiente
     */
    private DetalleCompra toDetalleDomain(CompraDetalleEntity entity) {
        RecursoId recursoId = RecursoId.of(entity.getRecursoId());
        Cantidad cantidad = Cantidad.of(entity.getCantidad());
        PrecioUnitario precioUnitario = PrecioUnitario.of(entity.getPrecioUnitario());

        return DetalleCompra.crear(recursoId, cantidad, precioUnitario);
    }
}
