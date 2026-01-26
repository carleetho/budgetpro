package com.budgetpro.infrastructure.rest.produccion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request para detalle de reporte de producción.
 */
public record DetalleRPCRequest(
        @NotNull(message = "La partidaId es obligatoria")
        UUID partidaId,

        @NotNull(message = "La cantidad reportada es obligatoria")
        @Positive(message = "La cantidad reportada debe ser positiva")
        BigDecimal cantidadReportada
) {
}
