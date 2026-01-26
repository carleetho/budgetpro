package com.budgetpro.domain.finanzas.estimacion.model;

/**
 * Enum que representa el estado de una Estimación.
 * 
 * Estados posibles:
 * - BORRADOR: Estimación en proceso de creación
 * - APROBADA: Estimación autorizada, lista para facturar
 * - PAGADA: Estimación pagada, ingreso registrado en billetera
 */
public enum EstadoEstimacion {
    BORRADOR,
    APROBADA,
    PAGADA
}
