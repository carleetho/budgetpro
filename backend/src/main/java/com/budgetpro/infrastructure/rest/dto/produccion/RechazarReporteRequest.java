package com.budgetpro.infrastructure.rest.dto.produccion;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para rechazar un reporte de producción.
 */
public record RechazarReporteRequest(
        // REGLA-141
        @NotBlank(message = "El motivo es obligatorio")
        String motivo
) {
}
