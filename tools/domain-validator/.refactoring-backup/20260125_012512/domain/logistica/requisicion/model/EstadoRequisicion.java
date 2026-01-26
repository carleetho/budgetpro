package com.budgetpro.domain.logistica.requisicion.model;

/**
 * Enum que representa el estado de una Requisición en su workflow.
 */
public enum EstadoRequisicion {
    /**
     * Requisición en borrador (aún no enviada).
     */
    BORRADOR,
    
    /**
     * Requisición solicitada (enviada para aprobación).
     */
    SOLICITADA,
    
    /**
     * Requisición aprobada (lista para despacho).
     */
    APROBADA,
    
    /**
     * Requisición con despacho parcial (algunos ítems despachados).
     */
    DESPACHADA_PARCIAL,
    
    /**
     * Requisición completamente despachada (todos los ítems completos).
     */
    DESPACHADA_TOTAL,
    
    /**
     * Requisición cerrada (proceso completado).
     */
    CERRADA,
    
    /**
     * Requisición rechazada (no aprobada).
     */
    RECHAZADA,
    
    /**
     * Requisición pendiente de compra (requiere compra externa).
     */
    PENDIENTE_COMPRA
}
