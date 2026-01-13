package com.budgetpro.domain.proyecto.model;

/**
 * Enum que representa los estados posibles de un Proyecto.
 * 
 * Estados válidos:
 * - BORRADOR: Proyecto en creación, aún no activo
 * - ACTIVO: Proyecto en ejecución
 * - SUSPENDIDO: Proyecto temporalmente detenido
 * - CERRADO: Proyecto finalizado
 */
public enum EstadoProyecto {
    BORRADOR,
    ACTIVO,
    SUSPENDIDO,
    CERRADO
}
