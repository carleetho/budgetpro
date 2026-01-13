package com.budgetpro.application.compra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de comando para un detalle de compra.
 */
public record CompraDetalleCommand(
        @NotNull(message = "El ID del recurso es obligatorio")
        UUID recursoId,
        
        @NotNull(message = "El ID de la partida es obligatorio")
        UUID partidaId,
        
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
        BigDecimal cantidad,
        
        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario
) {
}
