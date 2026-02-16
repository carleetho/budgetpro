package com.budgetpro.domain.logistica.compra.model;

/**
 * Enum que representa el estado de una orden de compra.
 * 
 * Máquina de estados secuencial:
 * BORRADOR → SOLICITADA → APROBADA → ENVIADA → RECIBIDA
 * 
 * Transiciones permitidas:
 * - BORRADOR → SOLICITADA (solicitar)
 * - SOLICITADA → APROBADA (aprobar)
 * - SOLICITADA → BORRADOR (rechazar)
 * - APROBADA → ENVIADA (enviar)
 * - ENVIADA → RECIBIDA (confirmarRecepcion)
 */
public enum OrdenCompraEstado {
    /**
     * Orden en borrador. Puede ser modificada o eliminada.
     */
    BORRADOR,
    
    /**
     * Orden solicitada para aprobación. No puede ser modificada.
     */
    SOLICITADA,
    
    /**
     * Orden aprobada. Lista para ser enviada al proveedor.
     */
    APROBADA,
    
    /**
     * Orden enviada al proveedor. En tránsito.
     */
    ENVIADA,
    
    /**
     * Orden recibida. Mercancía confirmada.
     */
    RECIBIDA
}
