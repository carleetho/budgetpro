package com.budgetpro.application.partida.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa el comando para crear una nueva partida.
 * 
 * Usado como entrada del caso de uso CrearPartidaUseCase.
 */
public record CrearPartidaCommand(
        @NotNull(message = "El ID del presupuesto es obligatorio")
        UUID presupuestoId,

        @NotBlank(message = "El código de la partida es obligatorio")
        String codigo,

        @NotBlank(message = "El nombre de la partida es obligatorio")
        String nombre,

        @NotBlank(message = "El tipo de la partida es obligatorio")
        String tipo,

        @NotNull(message = "El monto presupuestado es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto presupuestado no puede ser negativo")
        BigDecimal montoPresupuestado
) {
}
