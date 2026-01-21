package com.budgetpro.validator.model;

/**
 * Severidad de una violación detectada por el validador.
 */
public enum ViolationSeverity {
    /**
     * Violación crítica que bloquea el desarrollo.
     * Debe resolverse antes de continuar.
     */
    CRITICAL,
    
    /**
     * Advertencia que requiere revisión.
     * Puede continuarse con justificación explícita.
     */
    WARNING,
    
    /**
     * Información que no bloquea el desarrollo.
     * Útil para gobernanza y documentación.
     */
    INFO
}
