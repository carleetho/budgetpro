package com.budgetpro.domain.logistica.compra.port.out;

import java.util.UUID;

/**
 * Puerto de salida para validación de partidas presupuestarias.
 * 
 * Valida que las partidas sean válidas según REGLA-153.
 */
public interface PartidaValidator {

    /**
     * Verifica si una partida es una partida leaf válida (no tiene hijos).
     * 
     * REGLA-153: Cada línea de detalle debe referenciar una partida leaf válida.
     * 
     * @param partidaId ID de la partida a validar
     * @return true si la partida es válida y es leaf, false en caso contrario
     */
    boolean esPartidaLeafValida(UUID partidaId);
}
