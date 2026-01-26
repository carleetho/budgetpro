package com.budgetpro.infrastructure.persistence.mapper.alertas;

import com.budgetpro.domain.finanzas.alertas.model.AnalisisPresupuesto;
import com.budgetpro.domain.finanzas.alertas.model.AlertaParametrica;
import com.budgetpro.infrastructure.persistence.entity.alertas.AnalisisPresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.alertas.AlertaParametricaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre AnalisisPresupuesto (dominio) y AnalisisPresupuestoEntity (persistencia).
 */
@Component
public class AnalisisPresupuestoMapper {

    private final AlertaParametricaMapper alertaMapper;

    public AnalisisPresupuestoMapper(AlertaParametricaMapper alertaMapper) {
        this.alertaMapper = alertaMapper;
    }

    /**
     * Convierte un AnalisisPresupuesto (dominio) a AnalisisPresupuestoEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public AnalisisPresupuestoEntity toEntity(AnalisisPresupuesto analisis) {
        if (analisis == null) {
            return null;
        }

        AnalisisPresupuestoEntity entity = new AnalisisPresupuestoEntity(
            analisis.getId(),
            analisis.getPresupuestoId(),
            analisis.getFechaAnalisis(),
            analisis.getTotalAlertas(),
            analisis.getTotalAlertasCriticas(),
            analisis.getTotalAlertasWarning(),
            analisis.getTotalAlertasInfo(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );

        // Mapear alertas
        if (analisis.getAlertas() != null && !analisis.getAlertas().isEmpty()) {
            List<AlertaParametricaEntity> alertasEntity = analisis.getAlertas().stream()
                    .map(alerta -> alertaMapper.toEntity(alerta, entity))
                    .collect(Collectors.toList());
            entity.setAlertas(alertasEntity);
        }

        return entity;
    }

    /**
     * Convierte un AnalisisPresupuestoEntity (persistencia) a AnalisisPresupuesto (dominio).
     */
    public AnalisisPresupuesto toDomain(AnalisisPresupuestoEntity entity) {
        if (entity == null) {
            return null;
        }

        // Mapear alertas
        List<AlertaParametrica> alertas = entity.getAlertas().stream()
                .map(alertaMapper::toDomain)
                .collect(Collectors.toList());

        return AnalisisPresupuesto.reconstruir(
            entity.getId(),
            entity.getPresupuestoId(),
            entity.getFechaAnalisis(),
            alertas,
            entity.getVersion() != null ? entity.getVersion() : 0
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(AnalisisPresupuestoEntity existingEntity, AnalisisPresupuesto analisis) {
        existingEntity.setFechaAnalisis(analisis.getFechaAnalisis());
        existingEntity.setTotalAlertas(analisis.getTotalAlertas());
        existingEntity.setAlertasCriticas(analisis.getTotalAlertasCriticas());
        existingEntity.setAlertasWarning(analisis.getTotalAlertasWarning());
        existingEntity.setAlertasInfo(analisis.getTotalAlertasInfo());
        
        // Actualizar alertas: eliminar todas y agregar las nuevas
        existingEntity.getAlertas().clear();
        if (analisis.getAlertas() != null && !analisis.getAlertas().isEmpty()) {
            List<AlertaParametricaEntity> nuevasAlertas = analisis.getAlertas().stream()
                    .map(alerta -> alertaMapper.toEntity(alerta, existingEntity))
                    .collect(Collectors.toList());
            existingEntity.getAlertas().addAll(nuevasAlertas);
        }
        
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca presupuestoId (es inmutable después de crear)
    }
}
