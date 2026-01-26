package com.budgetpro.infrastructure.persistence.repository.evm;

import com.budgetpro.infrastructure.persistence.entity.evm.EVMSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para EVMSnapshotEntity.
 */
@Repository
public interface JpaEVMSnapshotRepository extends JpaRepository<EVMSnapshotEntity, UUID> {

    /**
     * Busca snapshots de un proyecto dentro de un rango de fechas de corte.
     */
    List<EVMSnapshotEntity> findByProyectoIdAndFechaCorteBetweenOrderByFechaCorteDesc(UUID proyectoId,
            LocalDateTime desde, LocalDateTime hasta);

    /**
     * Busca todos los snapshots de un proyecto ordenados por fecha de corte.
     */
    List<EVMSnapshotEntity> findByProyectoIdOrderByFechaCorteDesc(UUID proyectoId);

    /**
     * Busca la snapshot más reciente de un proyecto.
     */
    Optional<EVMSnapshotEntity> findFirstByProyectoIdOrderByFechaCorteDesc(UUID proyectoId);

    /**
     * Verifica si existe un snapshot para un proyecto y fecha de corte específica.
     */
    boolean existsByProyectoIdAndFechaCorte(UUID proyectoId, LocalDateTime fechaCorte);
}
