package com.budgetpro.domain.catalogo.port;

import com.budgetpro.domain.catalogo.model.APUSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de APUSnapshot.
 */
public interface ApuSnapshotRepository {

    Optional<APUSnapshot> findById(UUID id);

    Optional<APUSnapshot> findByPartidaId(UUID partidaId);

    APUSnapshot save(APUSnapshot snapshot);

    List<APUSnapshot> findModificados();
}
