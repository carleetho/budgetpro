package com.budgetpro.infrastructure.rest.compra.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request REST para un detalle de orden de compra.
 */
@Schema(description = "Detalle de una orden de compra")
public record DetalleOrdenCompraRequest(
        @Schema(description = "ID de la partida presupuestaria (debe ser leaf node)", required = true, example = "770e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El ID de la partida es obligatorio")
        UUID partidaId,
        
        @Schema(description = "Descripción del detalle", required = true, example = "Cemento Portland Tipo I")
        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,
        
        @Schema(description = "Cantidad del item", required = true, example = "100.00")
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        BigDecimal cantidad,
        
        @Schema(description = "Unidad de medida", example = "KG")
        String unidad,
        
        @Schema(description = "Precio unitario del item", required = true, example = "0.50")
        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario
) {
}
