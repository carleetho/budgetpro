package com.budgetpro.domain.logistica.inventario.model;

/**
 * Enum que representa los tipos de movimiento de inventario.
 * 
 * - ENTRADA_COMPRA: Entrada de material por compra
 * - SALIDA_CONSUMO: Salida de material por consumo/uso en obra
 * - AJUSTE: Ajuste de inventario (sobrantes, pérdidas, etc.)
 * - SALIDA_TRANSFERENCIA: Salida por transferencia entre bodegas
 * - ENTRADA_TRANSFERENCIA: Entrada por transferencia entre bodegas
 * - SALIDA_PRESTAMO: Salida por préstamo temporal
 * - ENTRADA_PRESTAMO: Entrada por devolución de préstamo
 */
public enum TipoMovimientoInventario {
    ENTRADA_COMPRA,
    SALIDA_CONSUMO,
    AJUSTE,
    SALIDA_TRANSFERENCIA,
    ENTRADA_TRANSFERENCIA,
    SALIDA_PRESTAMO,
    ENTRADA_PRESTAMO
}
