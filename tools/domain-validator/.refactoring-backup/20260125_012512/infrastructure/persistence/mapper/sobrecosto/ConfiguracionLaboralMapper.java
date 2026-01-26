package com.budgetpro.infrastructure.persistence.mapper.sobrecosto;

import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboralId;
import com.budgetpro.infrastructure.persistence.entity.sobrecosto.ConfiguracionLaboralEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre ConfiguracionLaboral (dominio) y ConfiguracionLaboralEntity (persistencia).
 */
@Component
public class ConfiguracionLaboralMapper {

    /**
     * Convierte un ConfiguracionLaboral (dominio) a ConfiguracionLaboralEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ConfiguracionLaboralEntity toEntity(ConfiguracionLaboral configuracion) {
        if (configuracion == null) {
            return null;
        }

        return new ConfiguracionLaboralEntity(
            configuracion.getId().getValue(),
            configuracion.getProyectoId(),
            configuracion.getDiasAguinaldo(),
            configuracion.getDiasVacaciones(),
            configuracion.getPorcentajeSeguridadSocial(),
            configuracion.getDiasNoTrabajados(),
            configuracion.getDiasLaborablesAno(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un ConfiguracionLaboralEntity (persistencia) a ConfiguracionLaboral (dominio).
     */
    public ConfiguracionLaboral toDomain(ConfiguracionLaboralEntity entity) {
        if (entity == null) {
            return null;
        }

        return ConfiguracionLaboral.reconstruir(
            ConfiguracionLaboralId.of(entity.getId()),
            entity.getProyectoId(),
            entity.getDiasAguinaldo(),
            entity.getDiasVacaciones(),
            entity.getPorcentajeSeguridadSocial(),
            entity.getDiasNoTrabajados(),
            entity.getDiasLaborablesAno(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(ConfiguracionLaboralEntity existingEntity, ConfiguracionLaboral configuracion) {
        existingEntity.setDiasAguinaldo(configuracion.getDiasAguinaldo());
        existingEntity.setDiasVacaciones(configuracion.getDiasVacaciones());
        existingEntity.setPorcentajeSeguridadSocial(configuracion.getPorcentajeSeguridadSocial());
        existingEntity.setDiasNoTrabajados(configuracion.getDiasNoTrabajados());
        existingEntity.setDiasLaborablesAno(configuracion.getDiasLaborablesAno());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca proyectoId (es inmutable después de crear)
    }
}
