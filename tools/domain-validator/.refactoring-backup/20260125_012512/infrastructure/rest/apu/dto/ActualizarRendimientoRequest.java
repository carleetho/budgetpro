package com.budgetpro.infrastructure.rest.apu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request para actualizar el rendimiento de un APU.
 */
public record ActualizarRendimientoRequest(
        @NotNull(message = "El nuevo rendimiento es obligatorio")
        @DecimalMin(value = "0.0001", message = "El rendimiento debe ser mayor a 0")
        BigDecimal nuevoRendimiento,
        
        @NotNull(message = "El ID del usuario es obligatorio")
        UUID usuarioId
) {
}
