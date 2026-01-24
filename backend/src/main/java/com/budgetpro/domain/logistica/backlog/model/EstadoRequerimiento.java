package com.budgetpro.domain.logistica.backlog.model;

/**
 * Enum que representa el estado de un RequerimientoCompra en su workflow.
 */
public enum EstadoRequerimiento {
    /**
     * Requerimiento pendiente: creado pero aún no procesado por Compras.
     */
    PENDIENTE,
    
    /**
     * Requerimiento en cotización: Compras está obteniendo cotizaciones.
     */
    EN_COTIZACION,
    
    /**
     * Requerimiento ordenado: se creó una orden de compra.
     */
    ORDENADA,
    
    /**
     * Requerimiento recibido: la compra llegó y se registró en inventario.
     */
    RECIBIDA,
    
    /**
     * Requerimiento cancelado: ya no es necesario.
     */
    CANCELADA
}
