package com.budgetpro.infrastructure.persistence.mapper.avance;

import com.budgetpro.domain.finanzas.avance.model.EstadoValuacion;
import com.budgetpro.domain.finanzas.avance.model.Valuacion;
import com.budgetpro.domain.finanzas.avance.model.ValuacionId;
import com.budgetpro.infrastructure.persistence.entity.avance.ValuacionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Valuacion (dominio) y ValuacionEntity (persistencia).
 */
@Component
public class ValuacionMapper {

    /**
     * Convierte un Valuacion (dominio) a ValuacionEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ValuacionEntity toEntity(Valuacion valuacion) {
        if (valuacion == null) {
            return null;
        }

        return new ValuacionEntity(
            valuacion.getId().getValue(),
            valuacion.getProyectoId(),
            valuacion.getFechaCorte(),
            valuacion.getCodigo(),
            valuacion.getEstado(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un ValuacionEntity (persistencia) a Valuacion (dominio).
     */
    public Valuacion toDomain(ValuacionEntity entity) {
        if (entity == null) {
            return null;
        }

        return Valuacion.reconstruir(
            ValuacionId.of(entity.getId()),
            entity.getProyectoId(),
            entity.getFechaCorte(),
            entity.getCodigo(),
            entity.getEstado(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(ValuacionEntity existingEntity, Valuacion valuacion) {
        existingEntity.setCodigo(valuacion.getCodigo());
        existingEntity.setEstado(valuacion.getEstado());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca proyectoId ni fechaCorte (son inmutables después de crear)
    }
}
