package com.budgetpro.domain.logistica.compra.port.out;

import com.budgetpro.domain.logistica.compra.model.ProveedorId;

/**
 * Puerto de salida para validación de proveedores.
 * 
 * Valida que el proveedor esté activo según regla L-04.
 */
public interface ProveedorValidator {

    /**
     * Verifica si un proveedor está activo y puede ser utilizado en compras.
     * 
     * L-04: Solo proveedores con estado=ACTIVO pueden ser utilizados en compras.
     * 
     * @param proveedorId ID del proveedor a validar
     * @return true si el proveedor está activo, false en caso contrario
     * @throws IllegalStateException si el proveedor no existe
     */
    boolean esProveedorActivo(ProveedorId proveedorId);
}
