package com.budgetpro.infrastructure.rest.apu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request REST para crear un APU.
 */
public record CrearApuRequest(
        BigDecimal rendimiento, // Opcional
        
        String unidad, // Opcional
        
        @NotNull(message = "La lista de insumos es obligatoria")
        @Valid
        List<ApuInsumoRequest> insumos
) {
}
