package com.budgetpro.domain.finanzas.alertas.model;

/**
 * Enum que representa el nivel de severidad de una alerta paramétrica.
 */
public enum NivelAlerta {
    /**
     * Alerta informativa. No requiere acción inmediata.
     */
    INFO,
    
    /**
     * Alerta de advertencia. Requiere revisión.
     */
    WARNING,
    
    /**
     * Alerta crítica. Requiere acción inmediata.
     */
    CRITICA
}
