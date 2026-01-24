package com.budgetpro.domain.logistica.inventario.port.out;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado InventarioItem.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas
 * (JPA, JDBC, etc.). La implementación estará en la capa de infraestructura.
 * 
 * REGLA: Este es un puerto puro del dominio. NO contiene anotaciones JPA/Spring.
 */
public interface InventarioRepository {

    /**
     * Busca un item de inventario por el ID del proyecto y el ID del recurso.
     * 
     * Cada proyecto tiene un item de inventario por recurso (relación 1:1 lógica).
     * 
     * @param proyectoId El ID del proyecto
     * @param recursoId El ID del recurso
     * @return Optional con el item de inventario si existe, vacío en caso contrario
     */
    Optional<InventarioItem> findByProyectoIdAndRecursoId(UUID proyectoId, UUID recursoId);

    /**
     * Busca un item de inventario por proyecto, recurso externo, unidad base y bodega.
     * Usado para find-or-create con variantes de unidad (KG vs LIBRAS coexisten).
     * 
     * @param proyectoId ID del proyecto
     * @param recursoExternalId ID externo del recurso (ej. "MAT-001")
     * @param unidadBase Unidad base del recurso (snapshot)
     * @param bodegaId ID de la bodega
     * @return Optional con el item si existe, vacío en caso contrario
     */
    Optional<InventarioItem> findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
            UUID proyectoId, String recursoExternalId, String unidadBase, BodegaId bodegaId);

    /**
     * Busca todos los items de inventario de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de items de inventario del proyecto
     */
    List<InventarioItem> findByProyectoId(UUID proyectoId);

    /**
     * Guarda un item de inventario y sus nuevos movimientos.
     * 
     * REGLA CRÍTICA: Este método debe:
     * 1. Persistir el item de inventario (con cantidad, costo promedio y versión actualizados)
     * 2. Persistir TODOS los movimientos nuevos del agregado
     * 3. Ejecutarse en una transacción ACID única
     * 4. Manejar optimistic locking usando el campo version
     * 
     * Después de persistir exitosamente, debe invocar inventarioItem.limpiarMovimientosNuevos()
     * para limpiar la lista de movimientos pendientes.
     * 
     * @param inventarioItem El item de inventario a guardar (con sus movimientos nuevos)
     */
    void save(InventarioItem inventarioItem);

    /**
     * Busca un item de inventario por su ID.
     * 
     * @param id El ID del item de inventario
     * @return Optional con el item de inventario si existe, vacío en caso contrario
     */
    Optional<InventarioItem> findById(InventarioId id);
}
