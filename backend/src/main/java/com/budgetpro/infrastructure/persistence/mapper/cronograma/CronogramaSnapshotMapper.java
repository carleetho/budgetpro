package com.budgetpro.infrastructure.persistence.mapper.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.cronograma.CronogramaSnapshotEntity;
import com.budgetpro.shared.validation.JsonSchemaValidator;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre CronogramaSnapshot (dominio) y CronogramaSnapshotEntity (persistencia).
 * 
 * Valida los JSON antes de persistir para asegurar que cumplan con los esquemas definidos.
 */
@Component
public class CronogramaSnapshotMapper {

    private final JsonSchemaValidator jsonSchemaValidator;

    public CronogramaSnapshotMapper(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    /**
     * Convierte un CronogramaSnapshot (dominio) a CronogramaSnapshotEntity (persistencia).
     * 
     * Valida los JSON antes de crear la entidad para asegurar que cumplan con los esquemas.
     * 
     * @param snapshot El snapshot del dominio
     * @return La entidad JPA
     * @throws IllegalArgumentException si algún JSON no cumple con el esquema
     */
    public CronogramaSnapshotEntity toEntity(CronogramaSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        // Validar esquemas JSON antes de persistir
        jsonSchemaValidator.validateFechasSnapshot(snapshot.getFechasJson());
        jsonSchemaValidator.validateDuracionesSnapshot(snapshot.getDuracionesJson());
        jsonSchemaValidator.validateSecuenciaSnapshot(snapshot.getSecuenciaJson());
        jsonSchemaValidator.validateCalendariosSnapshot(snapshot.getCalendariosJson());

        return new CronogramaSnapshotEntity(
            snapshot.getId().getValue(),
            snapshot.getProgramaObraId().getValue(),
            snapshot.getPresupuestoId().getValue(),
            snapshot.getFechasJson(),
            snapshot.getDuracionesJson(),
            snapshot.getSecuenciaJson(),
            snapshot.getCalendariosJson(),
            snapshot.getSnapshotDate(),
            snapshot.getSnapshotAlgorithm()
        );
    }

    /**
     * Convierte un CronogramaSnapshotEntity (persistencia) a CronogramaSnapshot (dominio).
     */
    public CronogramaSnapshot toDomain(CronogramaSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }

        return CronogramaSnapshot.reconstruir(
            CronogramaSnapshotId.of(entity.getId()),
            ProgramaObraId.of(entity.getProgramaObraId()),
            PresupuestoId.from(entity.getPresupuestoId()),
            entity.getFechasJson(),
            entity.getDuracionesJson(),
            entity.getSecuenciaJson(),
            entity.getCalendariosJson(),
            entity.getSnapshotDate(),
            entity.getSnapshotAlgorithm()
        );
    }
}
