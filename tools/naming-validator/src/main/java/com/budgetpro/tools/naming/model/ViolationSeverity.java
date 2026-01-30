package com.budgetpro.tools.naming.model;

/**
 * Severidad de una violación de convención de nombres.
 */
public enum ViolationSeverity {
    /** Bloqueante: El commit será rechazado. */
    BLOCKING,
    /** Advertencia: Se muestra el mensaje pero se permite continuar. */
    WARNING
}
