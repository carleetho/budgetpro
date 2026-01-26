package com.budgetpro.domain.finanzas.evm.port.out;

import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de snapshots EVM.
 */
public interface EVMSnapshotRepository {

    void save(EVMSnapshot snapshot);

    Optional<EVMSnapshot> findLatestByProyectoId(UUID proyectoId);

    List<EVMSnapshot> findByProyectoId(UUID proyectoId);

    List<EVMSnapshot> findByProyectoIdAndRango(UUID proyectoId, LocalDateTime desde, LocalDateTime hasta);

    boolean existsByProyectoIdAndFechaCorte(UUID proyectoId, LocalDateTime fechaCorte);
}
