package com.budgetpro.infrastructure.persistence.mapper.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramadaId;
import com.budgetpro.infrastructure.persistence.entity.cronograma.ActividadProgramadaEntity;
import com.budgetpro.infrastructure.persistence.entity.cronograma.DependenciaActividadEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre ActividadProgramada (dominio) y ActividadProgramadaEntity (persistencia).
 */
@Component
public class ActividadProgramadaMapper {

    /**
     * Convierte un ActividadProgramada (dominio) a ActividadProgramadaEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ActividadProgramadaEntity toEntity(ActividadProgramada actividad) {
        if (actividad == null) {
            return null;
        }

        ActividadProgramadaEntity entity = new ActividadProgramadaEntity(
            actividad.getId().getValue(),
            actividad.getPartidaId(),
            actividad.getProgramaObraId(),
            actividad.getFechaInicio(),
            actividad.getFechaFin(),
            actividad.getDuracionDias(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );

        // Mapear dependencias (predecesoras)
        if (actividad.getPredecesoras() != null && !actividad.getPredecesoras().isEmpty()) {
            List<DependenciaActividadEntity> dependencias = actividad.getPredecesoras().stream()
                    .map(predecesoraId -> new DependenciaActividadEntity(
                        UUID.randomUUID(),
                        entity,
                        predecesoraId
                    ))
                    .collect(Collectors.toList());
            entity.setDependencias(dependencias);
        }

        return entity;
    }

    /**
     * Convierte un ActividadProgramadaEntity (persistencia) a ActividadProgramada (dominio).
     */
    public ActividadProgramada toDomain(ActividadProgramadaEntity entity) {
        if (entity == null) {
            return null;
        }

        // Extraer IDs de predecesoras desde las dependencias
        List<UUID> predecesoras = entity.getDependencias().stream()
                .map(DependenciaActividadEntity::getActividadPredecesoraId)
                .collect(Collectors.toList());

        return ActividadProgramada.reconstruir(
            ActividadProgramadaId.of(entity.getId()),
            entity.getPartidaId(),
            entity.getProgramaObraId(),
            entity.getFechaInicio(),
            entity.getFechaFin(),
            entity.getDuracionDias(),
            predecesoras,
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(ActividadProgramadaEntity existingEntity, ActividadProgramada actividad) {
        existingEntity.setFechaInicio(actividad.getFechaInicio());
        existingEntity.setFechaFin(actividad.getFechaFin());
        existingEntity.setDuracionDias(actividad.getDuracionDias());
        
        // Actualizar dependencias: eliminar todas y agregar las nuevas
        existingEntity.getDependencias().clear();
        if (actividad.getPredecesoras() != null && !actividad.getPredecesoras().isEmpty()) {
            List<DependenciaActividadEntity> nuevasDependencias = actividad.getPredecesoras().stream()
                    .map(predecesoraId -> new DependenciaActividadEntity(
                        UUID.randomUUID(),
                        existingEntity,
                        predecesoraId
                    ))
                    .collect(Collectors.toList());
            existingEntity.getDependencias().addAll(nuevasDependencias);
        }
        
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca partidaId ni programaObraId (son inmutables después de crear)
    }
}
