package com.budgetpro.infrastructure.rest.dto.produccion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear un reporte de producción (RPC).
 */
public record CrearReporteRequest(
        @NotNull(message = "La fecha del reporte es obligatoria")
        LocalDate fechaReporte,

        // REGLA-139
        @NotEmpty(message = "Debe incluir al menos un item")
        List<@Valid DetalleItemRequest> items
) {
}
