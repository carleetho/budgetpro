package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.logistica.bodega.model.Bodega;
import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.infrastructure.persistence.entity.BodegaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Bodega (dominio) y BodegaEntity (persistencia).
 */
@Component
public class BodegaMapper {

    /**
     * Convierte una Bodega (dominio) a BodegaEntity (persistencia) para CREACIÓN.
     *
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public BodegaEntity toEntity(Bodega bodega) {
        if (bodega == null) {
            return null;
        }

        return new BodegaEntity(
                bodega.getId().getValue(),
                bodega.getProyectoId(),
                bodega.getCodigo(),
                bodega.getNombre(),
                bodega.getUbicacionFisica(),
                bodega.getResponsable(),
                bodega.isActiva(),
                bodega.getFechaCreacion(),
                null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte una BodegaEntity (persistencia) a Bodega (dominio).
     */
    public Bodega toDomain(BodegaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Bodega.reconstruir(
                BodegaId.of(entity.getId()),
                entity.getProyectoId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getUbicacionFisica(),
                entity.getResponsable(),
                entity.isActiva(),
                entity.getFechaCreacion(),
                entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     *
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     * No se modifican id, proyectoId, codigo ni fechaCreacion (inmutables).
     */
    public void updateEntity(BodegaEntity existingEntity, Bodega bodega) {
        existingEntity.setNombre(bodega.getNombre());
        existingEntity.setUbicacionFisica(bodega.getUbicacionFisica());
        existingEntity.setResponsable(bodega.getResponsable());
        existingEntity.setActiva(bodega.isActiva());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
    }
}
