package com.budgetpro.domain.finanzas.presupuesto.model;

/**
 * Enum que representa los estados posibles de un Presupuesto.
 * 
 * Estados válidos:
 * - BORRADOR: Presupuesto en creación/modificación
 * - CONGELADO: Presupuesto aprobado y listo para uso
 * - INVALIDADO: Presupuesto invalidado
 */
public enum EstadoPresupuesto {
    BORRADOR,
    CONGELADO,
    INVALIDADO
}
