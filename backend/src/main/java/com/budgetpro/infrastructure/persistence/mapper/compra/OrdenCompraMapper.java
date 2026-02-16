package com.budgetpro.infrastructure.persistence.mapper.compra;

import com.budgetpro.domain.logistica.compra.model.DetalleOrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.infrastructure.persistence.entity.compra.DetalleOrdenCompraEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.OrdenCompraEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.ProveedorEntity;
import com.budgetpro.infrastructure.persistence.repository.compra.ProveedorJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre OrdenCompra (dominio) y OrdenCompraEntity (persistencia).
 */
@Component
public class OrdenCompraMapper {

    private final ProveedorJpaRepository proveedorJpaRepository;

    public OrdenCompraMapper(ProveedorJpaRepository proveedorJpaRepository) {
        this.proveedorJpaRepository = proveedorJpaRepository;
    }

    /**
     * Convierte un OrdenCompra (dominio) a OrdenCompraEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public OrdenCompraEntity toEntity(OrdenCompra ordenCompra) {
        if (ordenCompra == null) {
            throw new IllegalArgumentException("OrdenCompra no puede ser null");
        }

        // Cargar ProveedorEntity
        ProveedorEntity proveedorEntity = proveedorJpaRepository.findById(ordenCompra.getProveedorId().getValue())
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Proveedor no encontrado: %s", ordenCompra.getProveedorId())
                ));

        OrdenCompraEntity entity = new OrdenCompraEntity(
            ordenCompra.getId().getValue(),
            ordenCompra.getNumero(),
            ordenCompra.getProyectoId(),
            proveedorEntity,
            ordenCompra.getFecha(),
            ordenCompra.getEstado(),
            ordenCompra.getMontoTotal(),
            ordenCompra.getCondicionesPago(),
            ordenCompra.getObservaciones(),
            null, // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
            ordenCompra.getCreatedBy(),
            ordenCompra.getUpdatedBy()
        );

        // Mapear detalles
        List<DetalleOrdenCompraEntity> detallesEntities = new java.util.ArrayList<>();
        int orden = 0;
        for (DetalleOrdenCompra detalle : ordenCompra.getDetalles()) {
            detallesEntities.add(toDetalleEntity(detalle, entity, orden++));
        }
        entity.setDetalles(detallesEntities);

        return entity;
    }

    /**
     * Convierte un DetalleOrdenCompra (dominio) a DetalleOrdenCompraEntity (persistencia).
     */
    public DetalleOrdenCompraEntity toDetalleEntity(DetalleOrdenCompra detalle, OrdenCompraEntity ordenCompraEntity, int orden) {
        if (detalle == null) {
            throw new IllegalArgumentException("DetalleOrdenCompra no puede ser null");
        }

        return new DetalleOrdenCompraEntity(
            java.util.UUID.randomUUID(), // Generar nuevo ID para el detalle
            ordenCompraEntity,
            detalle.getPartidaId(),
            detalle.getDescripcion(),
            detalle.getCantidad(),
            detalle.getUnidad(),
            detalle.getPrecioUnitario(),
            detalle.getSubtotal(),
            orden,
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un OrdenCompraEntity (persistencia) a OrdenCompra (dominio).
     */
    public OrdenCompra toDomain(OrdenCompraEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("OrdenCompraEntity no puede ser null");
        }

        // Mapear detalles
        List<DetalleOrdenCompra> detalles = entity.getDetalles().stream()
                .map(this::toDetalleDomain)
                .collect(Collectors.toList());

        return OrdenCompra.reconstruir(
            OrdenCompraId.from(entity.getId()),
            entity.getNumero(),
            entity.getProyectoId(),
            com.budgetpro.domain.logistica.compra.model.ProveedorId.from(entity.getProveedor().getId()),
            entity.getFecha(),
            entity.getEstado(),
            entity.getMontoTotal(),
            entity.getCondicionesPago(),
            entity.getObservaciones(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L,
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt(),
            detalles
        );
    }

    /**
     * Convierte un DetalleOrdenCompraEntity (persistencia) a DetalleOrdenCompra (dominio).
     */
    public DetalleOrdenCompra toDetalleDomain(DetalleOrdenCompraEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("DetalleOrdenCompraEntity no puede ser null");
        }

        return DetalleOrdenCompra.reconstruir(
            entity.getPartidaId(),
            entity.getDescripcion(),
            entity.getCantidad(),
            entity.getUnidad(),
            entity.getPrecioUnitario(),
            entity.getSubtotal()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(OrdenCompraEntity existingEntity, OrdenCompra ordenCompra) {
        existingEntity.setEstado(ordenCompra.getEstado());
        existingEntity.setMontoTotal(ordenCompra.getMontoTotal());
        existingEntity.setCondicionesPago(ordenCompra.getCondicionesPago());
        existingEntity.setObservaciones(ordenCompra.getObservaciones());
        existingEntity.setUpdatedBy(ordenCompra.getUpdatedBy());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca id, numero, proyectoId, proveedor, fecha, createdBy, createdAt (son inmutables)
        // Los detalles se manejan con cascade y orphanRemoval
    }
}
