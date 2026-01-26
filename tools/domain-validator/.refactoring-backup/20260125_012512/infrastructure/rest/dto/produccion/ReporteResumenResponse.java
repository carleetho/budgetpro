package com.budgetpro.infrastructure.rest.dto.produccion;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de respuesta resumida para listado de reportes.
 */
public record ReporteResumenResponse(
        UUID id,
        LocalDate fechaReporte,
        String estado,
        UUID responsableId,
        String responsableNombre,
        UUID aprobadorId,
        String aprobadorNombre,
        int totalItems
) {
}
