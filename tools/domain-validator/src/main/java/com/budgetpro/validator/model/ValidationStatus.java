package com.budgetpro.validator.model;

/**
 * Estado general de una validación.
 */
public enum ValidationStatus {
    /**
     * Validación pasada sin violaciones.
     */
    PASSED,
    
    /**
     * Advertencias detectadas (requiere revisión).
     */
    WARNINGS,
    
    /**
     * Violaciones críticas detectadas (bloquea CI/CD).
     */
    CRITICAL_VIOLATIONS,
    
    /**
     * Error durante el análisis (estructura de código inválida).
     */
    ERROR
}
