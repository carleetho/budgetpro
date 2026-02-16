package com.budgetpro.domain.logistica.compra.model;

/**
 * Enum que representa el estado de un proveedor.
 * 
 * Estados válidos:
 * - ACTIVO: Proveedor habilitado para realizar compras (Invariant L-04)
 * - INACTIVO: Proveedor deshabilitado temporalmente
 * - BLOQUEADO: Proveedor bloqueado por razones administrativas
 */
public enum ProveedorEstado {
    /**
     * Proveedor activo y habilitado para realizar compras.
     * Solo proveedores con este estado pueden ser utilizados en órdenes de compra.
     */
    ACTIVO,
    
    /**
     * Proveedor inactivo temporalmente.
     * No puede ser utilizado en nuevas compras hasta que sea reactivado.
     */
    INACTIVO,
    
    /**
     * Proveedor bloqueado por razones administrativas.
     * No puede ser utilizado en compras y requiere intervención manual para desbloquear.
     */
    BLOQUEADO
}
