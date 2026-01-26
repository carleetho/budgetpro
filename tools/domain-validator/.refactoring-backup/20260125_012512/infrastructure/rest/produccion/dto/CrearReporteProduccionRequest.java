package com.budgetpro.infrastructure.rest.produccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request para crear un reporte de producción.
 */
public record CrearReporteProduccionRequest(
        @NotNull(message = "La fecha del reporte es obligatoria")
        LocalDate fechaReporte,

        @NotNull(message = "El responsableId es obligatorio")
        UUID responsableId,

        String comentario,
        String ubicacionGps,

        @NotEmpty(message = "Debe incluir al menos un detalle")
        List<@Valid DetalleRPCRequest> detalles
) {
}
