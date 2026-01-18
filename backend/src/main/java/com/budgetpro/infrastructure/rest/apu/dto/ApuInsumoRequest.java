package com.budgetpro.infrastructure.rest.apu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request REST para un insumo del APU.
 */
public record ApuInsumoRequest(
        @NotNull(message = "El ID del recurso es obligatorio")
        UUID recursoId,
        
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
        BigDecimal cantidad,
        
        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario
) {
}
