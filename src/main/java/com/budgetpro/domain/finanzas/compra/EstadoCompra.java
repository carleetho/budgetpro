package com.budgetpro.domain.finanzas.compra;

/**
 * Enum que representa el estado de una Compra.
 * 
 * Estados transaccionales (para compras directas):
 * - PENDIENTE: Compra creada pero aún no procesada (estado inicial)
 * - CONFIRMADA: Compra procesada exitosamente y aplicada a inventario/billetera
 * - ERROR: Compra falló durante el procesamiento
 * 
 * Estados legados (mantenidos para compatibilidad):
 * - REGISTRADA: Compra registrada inicialmente (deprecado, usar PENDIENTE)
 * - PROCESADA: Compra procesada y aplicada (deprecado, usar CONFIRMADA)
 * - CANCELADA: Compra cancelada
 * 
 * Transiciones válidas para compras directas:
 * - PENDIENTE -> CONFIRMADA (tras éxito completo del procesamiento)
 * - PENDIENTE -> ERROR (ante excepción controlada)
 */
public enum EstadoCompra {
    PENDIENTE,
    CONFIRMADA,
    ERROR,
    REGISTRADA,  // Mantenido para compatibilidad
    PROCESADA,   // Mantenido para compatibilidad
    CANCELADA
}
