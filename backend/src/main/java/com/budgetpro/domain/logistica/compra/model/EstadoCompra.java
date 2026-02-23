package com.budgetpro.domain.logistica.compra.model;

/**
 * Enum que representa el estado de una compra.
 */
public enum EstadoCompra {
    /**
     * Compra en borrador (aún no aprobada).
     */
    BORRADOR,
    
    /**
     * Compra aprobada y registrada.
     */
    APROBADA,
    
    /**
     * Compra enviada al proveedor. La orden de compra ha sido enviada y está en tránsito.
     */
    ENVIADA,
    
    /**
     * Compra parcialmente recibida. Solo se ha recibido una parte de los productos solicitados.
     */
    PARCIAL,
    
    /**
     * Compra completamente recibida. Todos los productos han sido recibidos y confirmados.
     */
    RECIBIDA
}
