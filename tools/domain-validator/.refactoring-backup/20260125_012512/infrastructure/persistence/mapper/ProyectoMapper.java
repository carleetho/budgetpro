package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Proyecto (dominio) y ProyectoEntity (persistencia).
 */
@Component
public class ProyectoMapper {

    /**
     * Convierte un Proyecto (dominio) a ProyectoEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ProyectoEntity toEntity(Proyecto proyecto) {
        if (proyecto == null) {
            return null;
        }

        return new ProyectoEntity(
            proyecto.getId().getValue(),
            proyecto.getNombre(),
            proyecto.getUbicacion(),
            proyecto.getEstado(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un Proyecto (dominio) a ProyectoEntity (persistencia) para ACTUALIZACIÓN.
     * 
     * CRÍTICO: Copia la versión de la entidad existente para mantener el optimistic locking.
     */
    public ProyectoEntity toEntityForUpdate(Proyecto proyecto, ProyectoEntity existingEntity) {
        if (proyecto == null) {
            return null;
        }

        ProyectoEntity entity = new ProyectoEntity(
            proyecto.getId().getValue(),
            proyecto.getNombre(),
            proyecto.getUbicacion(),
            proyecto.getEstado(),
            existingEntity.getVersion() // CRÍTICO: Copiar versión de entidad existente
        );
        entity.setCreatedAt(existingEntity.getCreatedAt()); // Preservar createdAt
        return entity;
    }

    /**
     * Convierte un ProyectoEntity (persistencia) a Proyecto (dominio).
     */
    public Proyecto toDomain(ProyectoEntity entity) {
        if (entity == null) {
            return null;
        }

        return Proyecto.reconstruir(
            ProyectoId.from(entity.getId()),
            entity.getNombre(),
            entity.getUbicacion(),
            entity.getEstado()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(ProyectoEntity existingEntity, Proyecto proyecto) {
        existingEntity.setNombre(proyecto.getNombre());
        existingEntity.setUbicacion(proyecto.getUbicacion());
        existingEntity.setEstado(proyecto.getEstado());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
    }

    /**
     * Convierte un Proyecto (dominio) a ProyectoResponse (DTO de aplicación).
     * 
     * NOTA: Este método no tiene acceso a las fechas de la entidad.
     * Para obtener las fechas, usa toResponse(ProyectoEntity entity) en su lugar.
     */
    public ProyectoResponse toResponse(Proyecto proyecto) {
        if (proyecto == null) {
            return null;
        }

        return new ProyectoResponse(
            proyecto.getId().getValue(),
            proyecto.getNombre(),
            proyecto.getUbicacion(),
            proyecto.getEstado(),
            null, // createdAt - usar toResponse(ProyectoEntity) para obtener fechas
            null  // updatedAt - usar toResponse(ProyectoEntity) para obtener fechas
        );
    }

    /**
     * Convierte un ProyectoEntity (persistencia) a ProyectoResponse (DTO de aplicación).
     * Incluye las fechas de creación y actualización.
     */
    public ProyectoResponse toResponse(ProyectoEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProyectoResponse(
            entity.getId(),
            entity.getNombre(),
            entity.getUbicacion(),
            entity.getEstado(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
