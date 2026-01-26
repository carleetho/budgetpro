package com.budgetpro.infrastructure.rest.produccion.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para Reporte de Producción.
 */
public record ReporteProduccionResponse(
        UUID id,
        LocalDate fechaReporte,
        UUID responsableId,
        UUID aprobadorId,
        String estado,
        String comentario,
        String ubicacionGps,
        List<DetalleRPCResponse> detalles
) {
}
