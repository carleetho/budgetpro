package com.budgetpro.infrastructure.persistence.mapper.compra;

import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraDetalleEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Compra (dominio) y CompraEntity (persistencia).
 */
@Component
public class CompraMapper {

    public CompraMapper() {
        // Ya no se necesita RecursoJpaRepository porque usamos referencias externas
    }

    /**
     * Convierte un Compra (dominio) a CompraEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public CompraEntity toEntity(Compra compra) {
        if (compra == null) {
            return null;
        }

        CompraEntity entity = new CompraEntity(
            compra.getId().getValue(),
            compra.getProyectoId(),
            compra.getFecha(),
            compra.getProveedor(),
            compra.getEstado(),
            compra.getTotal(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );

        // Mapear detalles
        List<CompraDetalleEntity> detallesEntities = compra.getDetalles().stream()
                .map(detalle -> toDetalleEntity(detalle, entity))
                .collect(Collectors.toList());
        entity.setDetalles(detallesEntities);

        return entity;
    }

    /**
     * Convierte un CompraDetalle (dominio) a CompraDetalleEntity (persistencia).
     */
    public CompraDetalleEntity toDetalleEntity(CompraDetalle detalle, CompraEntity compraEntity) {
        if (detalle == null) {
            return null;
        }

        return new CompraDetalleEntity(
            detalle.getId().getValue(),
            compraEntity,
            detalle.getRecursoExternalId(),
            detalle.getRecursoNombre(),
            detalle.getPartidaId(),
            detalle.getNaturalezaGasto(),
            detalle.getRelacionContractual(),
            detalle.getRubroInsumo(),
            detalle.getCantidad(),
            detalle.getPrecioUnitario(),
            detalle.getSubtotal(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un CompraEntity (persistencia) a Compra (dominio).
     */
    public Compra toDomain(CompraEntity entity) {
        if (entity == null) {
            return null;
        }

        // Mapear detalles
        List<CompraDetalle> detalles = entity.getDetalles().stream()
                .map(this::toDetalleDomain)
                .collect(Collectors.toList());

        return Compra.reconstruir(
            CompraId.from(entity.getId()),
            entity.getProyectoId(),
            entity.getFecha(),
            entity.getProveedor(),
            entity.getEstado(),
            entity.getTotal(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L,
            detalles
        );
    }

    /**
     * Convierte un CompraDetalleEntity (persistencia) a CompraDetalle (dominio).
     */
    public CompraDetalle toDetalleDomain(CompraDetalleEntity entity) {
        if (entity == null) {
            return null;
        }

        return CompraDetalle.reconstruir(
            com.budgetpro.domain.logistica.compra.model.CompraDetalleId.from(entity.getId()),
            entity.getRecursoExternalId(),
            entity.getRecursoNombre(),
            entity.getPartidaId(),
            entity.getNaturalezaGasto(),
            entity.getRelacionContractual(),
            entity.getRubroInsumo(),
            entity.getCantidad(),
            entity.getPrecioUnitario(),
            entity.getSubtotal()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(CompraEntity existingEntity, Compra compra) {
        existingEntity.setFecha(compra.getFecha());
        existingEntity.setProveedor(compra.getProveedor());
        existingEntity.setEstado(compra.getEstado());
        existingEntity.setTotal(compra.getTotal());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca proyectoId (es inmutable después de crear)
        // Los detalles se manejan con cascade y orphanRemoval
    }

    /**
     * Método obsoleto: Ya no se necesita asignar recursos ya que usamos referencias externas.
     * Se mantiene por compatibilidad pero no hace nada.
     * 
     * @deprecated Los recursos ahora se manejan mediante external_id, no se necesita cargar entidades.
     */
    @Deprecated
    public void asignarRecursosADetalles(CompraEntity entity, Compra compra) {
        // Ya no es necesario cargar RecursoEntity, los detalles ya tienen recursoExternalId y recursoNombre
        // Este método se mantiene por compatibilidad pero no hace nada
    }

}
