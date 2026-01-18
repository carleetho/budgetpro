package com.budgetpro.application.apu.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de comando para crear un nuevo APU.
 */
public record CrearApuCommand(
        @NotNull(message = "El ID de la partida es obligatorio")
        UUID partidaId,
        
        BigDecimal rendimiento, // Opcional
        
        String unidad, // Opcional
        
        @NotNull(message = "La lista de insumos es obligatoria")
        @Valid
        List<ApuInsumoCommand> insumos
) {
}
