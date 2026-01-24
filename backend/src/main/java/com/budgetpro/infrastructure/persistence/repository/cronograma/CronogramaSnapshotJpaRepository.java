package com.budgetpro.infrastructure.persistence.repository.cronograma;

import com.budgetpro.infrastructure.persistence.entity.cronograma.CronogramaSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para CronogramaSnapshotEntity.
 */
@Repository
public interface CronogramaSnapshotJpaRepository extends JpaRepository<CronogramaSnapshotEntity, UUID> {

    /**
     * Busca el snapshot más reciente de un programa de obra.
     * 
     * @param programaObraId El ID del programa de obra
     * @return Optional con el snapshot más reciente si existe
     */
    Optional<CronogramaSnapshotEntity> findFirstByProgramaObraIdOrderBySnapshotDateDesc(UUID programaObraId);

    /**
     * Busca todos los snapshots de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de snapshots ordenados por fecha descendente
     */
    List<CronogramaSnapshotEntity> findByPresupuestoIdOrderBySnapshotDateDesc(UUID presupuestoId);

    /**
     * Busca todos los snapshots de un programa de obra.
     * 
     * @param programaObraId El ID del programa de obra
     * @return Lista de snapshots ordenados por fecha descendente
     */
    List<CronogramaSnapshotEntity> findByProgramaObraIdOrderBySnapshotDateDesc(UUID programaObraId);
}
