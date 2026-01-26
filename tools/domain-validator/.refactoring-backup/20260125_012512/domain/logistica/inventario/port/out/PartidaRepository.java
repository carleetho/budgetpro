package com.budgetpro.domain.logistica.inventario.port.out;

import java.util.UUID;

public interface PartidaRepository {
    boolean existsById(UUID partidaId);

    boolean isPresupuestoCongelado(UUID partidaId);
}
