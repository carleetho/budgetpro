package com.budgetpro.infrastructure.rest.produccion.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de request para aprobar un reporte de producción.
 */
public record AprobarReporteRequest(
        @NotNull(message = "El aprobadorId es obligatorio")
        UUID aprobadorId
) {
}
