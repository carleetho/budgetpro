package com.budgetpro.domain.logistica.compra.port.out;

import com.budgetpro.domain.logistica.compra.model.Proveedor;
import com.budgetpro.domain.logistica.compra.model.ProveedorId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Proveedor.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface ProveedorRepository {

    /**
     * Guarda un proveedor.
     * 
     * @param proveedor El proveedor a guardar
     */
    void save(Proveedor proveedor);

    /**
     * Busca un proveedor por su ID.
     * 
     * @param id El ID del proveedor
     * @return Optional con el proveedor si existe, vacío en caso contrario
     */
    Optional<Proveedor> findById(ProveedorId id);

    /**
     * Obtiene todos los proveedores.
     * 
     * @return Lista de todos los proveedores
     */
    List<Proveedor> findAll();

    /**
     * Elimina un proveedor por su ID.
     * 
     * @param id El ID del proveedor a eliminar
     * @throws IllegalStateException si el proveedor está referenciado por órdenes de compra
     */
    void delete(ProveedorId id);

    /**
     * Verifica si existe un proveedor con el RUC dado.
     * 
     * @param ruc El RUC del proveedor
     * @return true si existe, false en caso contrario
     */
    boolean existsByRuc(String ruc);

    /**
     * Verifica si un proveedor está referenciado por alguna orden de compra.
     * 
     * @param id El ID del proveedor
     * @return true si está referenciado, false en caso contrario
     */
    boolean isReferencedByOrdenCompra(ProveedorId id);
}
