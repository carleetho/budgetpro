package com.budgetpro.infrastructure.persistence.mapper.compra;

import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraDetalleEntity;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Compra (dominio) y CompraEntity (persistencia).
 */
@Component
public class CompraMapper {

    private final RecursoJpaRepository recursoJpaRepository;

    public CompraMapper(RecursoJpaRepository recursoJpaRepository) {
        this.recursoJpaRepository = recursoJpaRepository;
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
                .map(detalle -> toDetalleEntity(detalle, entity, null)) // recursoEntity se carga después
                .collect(Collectors.toList());
        entity.setDetalles(detallesEntities);

        return entity;
    }

    /**
     * Convierte un CompraDetalle (dominio) a CompraDetalleEntity (persistencia).
     */
    public CompraDetalleEntity toDetalleEntity(CompraDetalle detalle, CompraEntity compraEntity, RecursoEntity recursoEntity) {
        if (detalle == null) {
            return null;
        }

        return new CompraDetalleEntity(
            detalle.getId().getValue(),
            compraEntity,
            recursoEntity, // Debe ser cargado antes de llamar este método
            detalle.getPartidaId(),
            detalle.getCantidad(),
            detalle.getPrecioUnitario(),
            detalle.getSubtotal(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
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

        UUID recursoId = entity.getRecurso() != null ? entity.getRecurso().getId() : null;

        return CompraDetalle.reconstruir(
            com.budgetpro.domain.logistica.compra.model.CompraDetalleId.from(entity.getId()),
            recursoId,
            entity.getPartidaId(),
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
     * Asigna los recursos a los detalles de la entidad.
     */
    public void asignarRecursosADetalles(CompraEntity entity, Compra compra) {
        List<CompraDetalleEntity> detallesEntities = entity.getDetalles();
        List<CompraDetalle> detallesDomain = compra.getDetalles();

        for (int i = 0; i < detallesEntities.size() && i < detallesDomain.size(); i++) {
            CompraDetalleEntity detalleEntity = detallesEntities.get(i);
            CompraDetalle detalleDomain = detallesDomain.get(i);

            RecursoEntity recursoEntity = recursoJpaRepository.findById(detalleDomain.getRecursoId())
                    .orElseThrow(() -> new IllegalStateException("Recurso no encontrado: " + detalleDomain.getRecursoId()));

            detalleEntity.setRecurso(recursoEntity);
        }
    }

}
