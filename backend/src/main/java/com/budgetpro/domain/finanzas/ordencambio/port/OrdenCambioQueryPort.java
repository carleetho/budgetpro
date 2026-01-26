package com.budgetpro.domain.finanzas.ordencambio.port;

import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto para consultas complejas de órdenes de cambio (CQRS - Query side).
 */
public interface OrdenCambioQueryPort {

    /**
     * Lista todas las órdenes de cambio de un proyecto.
     *
     * @param proyectoId ID del proyecto
     * @return Lista de órdenes (puede ser vacía)
     */
    List<OrdenCambio> findByProyectoId(UUID proyectoId);

    /**
     * Busca órdenes de cambio en un proyecto aplicando filtros.
     *
     * @param proyectoId ID del proyecto
     * @param filters    Criterios de filtrado
     * @return Lista de órdenes filtrada
     */
    List<OrdenCambio> findByProyectoIdAndFilters(UUID proyectoId, OrdenCambioFilters filters);

    /**
     * Obtiene un resumen estadístico de las órdenes de cambio de un proyecto.
     *
     * @param proyectoId ID del proyecto
     * @return DTO con el resumen
     */
    ResumenOrdenCambio getResumenByProyecto(UUID proyectoId);

    /**
     * Obtiene una orden de cambio con todos sus detalles cargados (eager loading).
     * Incluye partidas, documentos, recursos e historial.
     *
     * @param id ID de la orden
     * @return Optional con la orden detallada si existe
     */
    Optional<OrdenCambio> findByIdWithDetails(OrdenCambioId id);
}
