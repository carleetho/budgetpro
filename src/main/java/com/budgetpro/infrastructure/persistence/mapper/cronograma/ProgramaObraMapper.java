package com.budgetpro.infrastructure.persistence.mapper.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.infrastructure.persistence.entity.cronograma.ProgramaObraEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre ProgramaObra (dominio) y ProgramaObraEntity (persistencia).
 */
@Component
public class ProgramaObraMapper {

    /**
     * Convierte un ProgramaObra (dominio) a ProgramaObraEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ProgramaObraEntity toEntity(ProgramaObra programaObra) {
        if (programaObra == null) {
            return null;
        }

        return new ProgramaObraEntity(
            programaObra.getId().getValue(),
            programaObra.getProyectoId(),
            programaObra.getFechaInicio(),
            programaObra.getFechaFinEstimada(),
            programaObra.getDuracionTotalDias(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un ProgramaObraEntity (persistencia) a ProgramaObra (dominio).
     */
    public ProgramaObra toDomain(ProgramaObraEntity entity) {
        if (entity == null) {
            return null;
        }

        return ProgramaObra.reconstruir(
            ProgramaObraId.of(entity.getId()),
            entity.getProyectoId(),
            entity.getFechaInicio(),
            entity.getFechaFinEstimada(),
            entity.getDuracionTotalDias(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(ProgramaObraEntity existingEntity, ProgramaObra programaObra) {
        existingEntity.setFechaInicio(programaObra.getFechaInicio());
        existingEntity.setFechaFinEstimada(programaObra.getFechaFinEstimada());
        existingEntity.setDuracionTotalDias(programaObra.getDuracionTotalDias());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca proyectoId (es inmutable después de crear)
    }
}
