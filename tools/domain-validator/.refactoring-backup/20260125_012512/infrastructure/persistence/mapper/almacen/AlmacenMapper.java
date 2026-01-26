package com.budgetpro.infrastructure.persistence.mapper.almacen;

import com.budgetpro.domain.logistica.almacen.model.Almacen;
import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.infrastructure.persistence.entity.almacen.AlmacenEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Almacen (dominio) y AlmacenEntity (persistencia).
 */
@Component
public class AlmacenMapper {

    /**
     * Convierte un Almacen (dominio) a AlmacenEntity (persistencia) para CREACIÓN.
     */
    public AlmacenEntity toEntity(Almacen almacen) {
        if (almacen == null) {
            return null;
        }

        return new AlmacenEntity(
            almacen.getId().getValue(),
            almacen.getProyectoId(),
            almacen.getCodigo(),
            almacen.getNombre(),
            almacen.getUbicacion(),
            almacen.getResponsableId(),
            almacen.isActivo(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un AlmacenEntity (persistencia) a Almacen (dominio).
     */
    public Almacen toDomain(AlmacenEntity entity) {
        if (entity == null) {
            return null;
        }

        return Almacen.reconstruir(
            AlmacenId.of(entity.getId()),
            entity.getProyectoId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getUbicacion(),
            entity.getResponsableId(),
            entity.getActivo()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     */
    public void updateEntity(AlmacenEntity existingEntity, Almacen almacen) {
        existingEntity.setNombre(almacen.getNombre());
        existingEntity.setUbicacion(almacen.getUbicacion());
        existingEntity.setResponsableId(almacen.getResponsableId());
        existingEntity.setActivo(almacen.isActivo());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se tocan campos inmutables (proyectoId, codigo)
    }
}
