package com.budgetpro.domain.finanzas.partida;

/**
 * Enum que representa el estado de una Partida.
 * 
 * BORRADOR: Partida en edición, no aprobada
 * APROBADA: Partida aprobada y lista para ejecución
 * CERRADA: Partida cerrada, no se pueden hacer más cambios
 */
public enum EstadoPartida {
    BORRADOR,
    APROBADA,
    CERRADA
}
