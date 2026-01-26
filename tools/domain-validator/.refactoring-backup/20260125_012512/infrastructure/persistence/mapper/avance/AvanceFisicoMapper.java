package com.budgetpro.infrastructure.persistence.mapper.avance;

import com.budgetpro.domain.finanzas.avance.model.AvanceFisico;
import com.budgetpro.domain.finanzas.avance.model.AvanceFisicoId;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.avance.AvanceFisicoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre AvanceFisico (dominio) y AvanceFisicoEntity (persistencia).
 */
@Component
public class AvanceFisicoMapper {

    /**
     * Convierte un AvanceFisico (dominio) a AvanceFisicoEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public AvanceFisicoEntity toEntity(AvanceFisico avance, PartidaEntity partidaEntity) {
        if (avance == null) {
            return null;
        }

        return new AvanceFisicoEntity(
            avance.getId().getValue(),
            partidaEntity,
            avance.getFecha(),
            avance.getMetradoEjecutado(),
            avance.getObservacion(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un AvanceFisicoEntity (persistencia) a AvanceFisico (dominio).
     */
    public AvanceFisico toDomain(AvanceFisicoEntity entity) {
        if (entity == null) {
            return null;
        }

        return AvanceFisico.reconstruir(
            AvanceFisicoId.of(entity.getId()),
            entity.getPartida().getId(),
            entity.getFecha(),
            entity.getMetradoEjecutado(),
            entity.getObservacion(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(AvanceFisicoEntity existingEntity, AvanceFisico avance) {
        existingEntity.setObservacion(avance.getObservacion());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca fecha ni metradoEjecutado (son inmutables después de crear)
    }
}
