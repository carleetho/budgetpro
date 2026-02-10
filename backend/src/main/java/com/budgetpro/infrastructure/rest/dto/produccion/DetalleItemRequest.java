package com.budgetpro.infrastructure.rest.dto.produccion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item de detalle para reporte de producción.
 */
public record DetalleItemRequest(
        @NotNull(message = "La partidaId es obligatoria")
        UUID partidaId,

        @NotNull(message = "La cantidad es obligatoria")
        // REGLA-140
        @Positive(message = "La cantidad debe ser positiva")
        BigDecimal cantidad
) {
}
