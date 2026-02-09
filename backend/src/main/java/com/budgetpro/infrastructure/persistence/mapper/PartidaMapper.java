package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mapper para convertir entre Partida (dominio) y PartidaEntity (persistencia).
 */
@Component
public class PartidaMapper {

    /**
     * Convierte un Partida (dominio) a PartidaEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public PartidaEntity toEntity(Partida partida, PresupuestoEntity presupuestoEntity, PartidaEntity padreEntity) {
        if (partida == null) {
            throw new IllegalArgumentException("Partida domain object cannot be null");
        }

        PartidaEntity entity = new PartidaEntity(
            partida.getId().getValue(),
            presupuestoEntity,
            padreEntity, // Puede ser null para partida raíz
            partida.getItem(),
            partida.getDescripcion(),
            partida.getUnidad(),
            partida.getMetrado(),
            partida.getNivel(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
        entity.setGastosReales(partida.getGastosReales());
        entity.setCompromisosPendientes(partida.getCompromisosPendientes());
        return entity;
    }

    /**
     * Convierte un PartidaEntity (persistencia) a Partida (dominio).
     */
    public Partida toDomain(PartidaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("PartidaEntity cannot be null");
        }

        UUID padreId = entity.getPadre() != null ? entity.getPadre().getId() : null;
        UUID presupuestoId = entity.getPresupuesto() != null ? entity.getPresupuesto().getId() : null;

        BigDecimal presupuestoAsignado = entity.getPrecioUnitario() != null && entity.getMetrado() != null
                ? entity.getPrecioUnitario().multiply(entity.getMetrado())
                : BigDecimal.ZERO;

        Partida partida = Partida.reconstruir(
            PartidaId.from(entity.getId()),
            presupuestoId,
            padreId,
            entity.getItem(),
            entity.getDescripcion(),
            entity.getUnidad(),
            entity.getMetrado(),
            presupuestoAsignado,
            entity.getGastosReales(),
            entity.getCompromisosPendientes(),
            entity.getNivel(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
        return partida;
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(PartidaEntity existingEntity, Partida partida) {
        existingEntity.setItem(partida.getItem());
        existingEntity.setDescripcion(partida.getDescripcion());
        existingEntity.setUnidad(partida.getUnidad());
        existingEntity.setMetrado(partida.getMetrado());
        existingEntity.setNivel(partida.getNivel());
        existingEntity.setGastosReales(partida.getGastosReales());
        existingEntity.setCompromisosPendientes(partida.getCompromisosPendientes());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca padre ni presupuesto (son inmutables después de crear)
    }
}
