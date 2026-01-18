package com.budgetpro.infrastructure.rest.dto.produccion;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta completo para reporte de producción.
 */
public record ReporteResponse(
        UUID id,
        LocalDate fechaReporte,
        String estado,
        UUID responsableId,
        String responsableNombre,
        UUID aprobadorId,
        String aprobadorNombre,
        String comentario,
        String ubicacionGps,
        List<DetalleItemResponse> items
) {
}
