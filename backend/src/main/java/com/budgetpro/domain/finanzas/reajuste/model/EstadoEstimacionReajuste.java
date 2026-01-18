package com.budgetpro.domain.finanzas.reajuste.model;

/**
 * Enum que representa el estado de una estimación de reajuste.
 */
public enum EstadoEstimacionReajuste {
    /**
     * Estimación calculada pero no aprobada.
     */
    BORRADOR,
    
    /**
     * Estimación aprobada, lista para aplicar.
     */
    APROBADA,
    
    /**
     * Estimación aplicada al presupuesto.
     */
    APLICADA
}
