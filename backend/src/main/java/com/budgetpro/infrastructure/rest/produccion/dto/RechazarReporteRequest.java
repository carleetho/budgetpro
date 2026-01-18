package com.budgetpro.infrastructure.rest.produccion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de request para rechazar un reporte de producción.
 */
public record RechazarReporteRequest(
        @NotNull(message = "El aprobadorId es obligatorio")
        UUID aprobadorId,

        @NotBlank(message = "El motivo de rechazo es obligatorio")
        String motivo
) {
}
