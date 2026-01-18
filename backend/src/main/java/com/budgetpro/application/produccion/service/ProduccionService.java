package com.budgetpro.application.produccion.service;

import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;

import java.util.UUID;

/**
 * Servicio de dominio/aplicación para gestión de Reportes de Producción (RPC).
 */
public interface ProduccionService {

    ReporteProduccionEntity crearReporte(ReporteProduccionEntity reporte);

    ReporteProduccionEntity actualizarReporte(UUID reporteId, ReporteProduccionEntity reporte);

    void eliminarReporte(UUID reporteId);

    ReporteProduccionEntity aprobarReporte(UUID reporteId, UUID usuarioAprobadorId);

    ReporteProduccionEntity rechazarReporte(UUID reporteId, UUID usuarioRechazoId, String motivo);
}
