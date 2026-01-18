package com.budgetpro.domain.logistica.inventario.model;

/**
 * Enum que representa los tipos de movimiento de inventario.
 * 
 * - ENTRADA_COMPRA: Entrada de material por compra
 * - SALIDA_CONSUMO: Salida de material por consumo/uso en obra
 * - AJUSTE: Ajuste de inventario (sobrantes, pérdidas, etc.)
 */
public enum TipoMovimientoInventario {
    ENTRADA_COMPRA,
    SALIDA_CONSUMO,
    AJUSTE
}
