package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Presupuesto (dominio) y PresupuestoEntity (persistencia).
 */
@Component
public class PresupuestoMapper {

    /**
     * Convierte un Presupuesto (dominio) a PresupuestoEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public PresupuestoEntity toEntity(Presupuesto presupuesto) {
        if (presupuesto == null) {
            return null;
        }

        return new PresupuestoEntity(
            presupuesto.getId().getValue(),
            presupuesto.getProyectoId(),
            presupuesto.getNombre(),
            presupuesto.getEstado(),
            presupuesto.getEsContractual(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un PresupuestoEntity (persistencia) a Presupuesto (dominio).
     */
    public Presupuesto toDomain(PresupuestoEntity entity) {
        if (entity == null) {
            return null;
        }

        return Presupuesto.reconstruir(
            PresupuestoId.from(entity.getId()),
            entity.getProyectoId(),
            entity.getNombre(),
            entity.getEstado(),
            entity.getEsContractual(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(PresupuestoEntity existingEntity, Presupuesto presupuesto) {
        existingEntity.setNombre(presupuesto.getNombre());
        existingEntity.setEstado(presupuesto.getEstado());
        existingEntity.setEsContractual(presupuesto.getEsContractual());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
    }
}
