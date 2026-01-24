package com.budgetpro.domain.logistica.backlog.port.out;

import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompra;
import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompraId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Salida (Outbound Port) para la persistencia de RequerimientoCompra.
 * 
 * Define el contrato de persistencia sin depender de tecnologías específicas.
 */
public interface RequerimientoCompraRepository {

    /**
     * Guarda un requerimiento de compra.
     * 
     * @param requerimiento El requerimiento a guardar
     */
    void save(RequerimientoCompra requerimiento);

    /**
     * Busca un requerimiento por su ID.
     * 
     * @param id El ID del requerimiento
     * @return Optional con el requerimiento si existe
     */
    Optional<RequerimientoCompra> findById(RequerimientoCompraId id);

    /**
     * Busca requerimientos pendientes para un recurso específico en un proyecto.
     * Usado para resolver backlog cuando llega stock.
     * 
     * @param proyectoId ID del proyecto
     * @param recursoExternalId ID externo del recurso
     * @param unidadMedida Unidad de medida
     * @return Lista de requerimientos pendientes (estado PENDIENTE, EN_COTIZACION, ORDENADA)
     */
    List<RequerimientoCompra> findPendientesPorRecurso(UUID proyectoId, String recursoExternalId, String unidadMedida);

    /**
     * Busca requerimientos por requisición origen.
     * 
     * @param requisicionId ID de la requisición
     * @return Lista de requerimientos generados por esa requisición
     */
    List<RequerimientoCompra> findByRequisicionId(UUID requisicionId);
}
