package com.budgetpro.domain.finanzas.cronograma.port.out;

import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

import java.util.Optional;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado CronogramaSnapshot.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface CronogramaSnapshotRepository {

    /**
     * Guarda un snapshot del cronograma.
     * 
     * @param snapshot El snapshot a guardar
     */
    void save(CronogramaSnapshot snapshot);

    /**
     * Busca un snapshot por su ID.
     * 
     * @param id El ID del snapshot
     * @return Optional con el snapshot si existe, vacío en caso contrario
     */
    Optional<CronogramaSnapshot> findById(CronogramaSnapshotId id);

    /**
     * Busca el snapshot asociado a un programa de obra (relación 1:1).
     * 
     * @param programaObraId El ID del programa de obra
     * @return Optional con el snapshot si existe, vacío en caso contrario
     */
    Optional<CronogramaSnapshot> findByProgramaObraId(ProgramaObraId programaObraId);

    /**
     * Busca todos los snapshots asociados a un presupuesto (relación many-to-one).
     * Útil para casos de re-baseline donde puede haber múltiples snapshots.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de snapshots asociados al presupuesto (puede estar vacía)
     */
    java.util.List<CronogramaSnapshot> findByPresupuestoId(PresupuestoId presupuestoId);
}
