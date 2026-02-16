package com.budgetpro.domain.logistica.compra.port.out;

import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado OrdenCompra.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 * La implementación estará en la capa de infraestructura.
 */
public interface OrdenCompraRepository {

    /**
     * Guarda una orden de compra.
     * 
     * @param ordenCompra La orden de compra a guardar
     */
    void save(OrdenCompra ordenCompra);

    /**
     * Busca una orden de compra por su ID.
     * 
     * @param id El ID de la orden de compra
     * @return Optional con la orden de compra si existe, vacío en caso contrario
     */
    Optional<OrdenCompra> findById(OrdenCompraId id);

    /**
     * Elimina una orden de compra por su ID.
     * 
     * @param id El ID de la orden de compra a eliminar
     */
    void delete(OrdenCompraId id);

    /**
     * Busca todas las órdenes de compra de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de órdenes de compra del proyecto
     */
    List<OrdenCompra> findByProyectoId(UUID proyectoId);

    /**
     * Busca todas las órdenes de compra con un estado específico.
     * 
     * @param estado El estado de la orden de compra
     * @return Lista de órdenes de compra con el estado especificado
     */
    List<OrdenCompra> findByEstado(OrdenCompraEstado estado);

    /**
     * Busca todas las órdenes de compra de un proyecto con un estado específico.
     * 
     * @param proyectoId El ID del proyecto
     * @param estado El estado de la orden de compra
     * @return Lista de órdenes de compra del proyecto con el estado especificado
     */
    List<OrdenCompra> findByProyectoIdAndEstado(UUID proyectoId, OrdenCompraEstado estado);

    /**
     * Genera el siguiente número secuencial de orden de compra para un año dado.
     * 
     * @param year El año para el cual generar el número
     * @return El siguiente número secuencial (ej. "PO-2024-001")
     */
    String generateNextNumero(int year);
}
