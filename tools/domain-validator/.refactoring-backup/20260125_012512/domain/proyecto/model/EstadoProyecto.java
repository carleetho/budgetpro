package com.budgetpro.domain.proyecto.model;

/**
 * Enum que representa los estados posibles de un Proyecto.
 * 
 * Estados válidos:
 * - BORRADOR: Proyecto en creación
 * - ACTIVO: Proyecto en ejecución contractual
 * - SUSPENDIDO: Proyecto detenido temporalmente
 * - CERRADO: Proyecto finalizado
 */
public enum EstadoProyecto {
    BORRADOR,
    ACTIVO,
    SUSPENDIDO,
    CERRADO
}
