package com.budgetpro.domain.finanzas.partida;

/**
 * Enum que representa el estado de una Partida presupuestaria.
 * 
 * Estados:
 * - BORRADOR: Partida en edición, no aprobada (estado por defecto)
 * - APROBADA: Partida aprobada y lista para ejecución
 * - CERRADA: Partida cerrada, no se pueden hacer más cambios
 * 
 * Transiciones:
 * - BORRADOR -> APROBADA (mediante aprobar())
 * - BORRADOR o APROBADA -> CERRADA (mediante cerrar())
 */
public enum EstadoPartida {
    BORRADOR,
    APROBADA,
    CERRADA
}
