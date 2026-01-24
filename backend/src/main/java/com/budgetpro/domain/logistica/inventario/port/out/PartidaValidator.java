package com.budgetpro.domain.logistica.inventario.port.out;

import java.util.UUID;

/**
 * Puerto para validar la existencia y estado de Partidas Presupuestales.
 */
public interface PartidaValidator {
    boolean existeYEstaActiva(UUID partidaId);

    double getPorcentajeEjecucion(UUID partidaId);
}
