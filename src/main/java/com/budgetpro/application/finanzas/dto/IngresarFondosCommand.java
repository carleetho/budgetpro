package com.budgetpro.application.finanzas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa el comando para ingresar fondos a una billetera.
 * 
 * Usado como entrada del caso de uso IngresarFondosUseCase.
 */
public record IngresarFondosCommand(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0001", message = "El monto debe ser mayor que cero", inclusive = false)
        BigDecimal monto,

        @NotBlank(message = "La referencia es obligatoria")
        String referencia,

        String evidenciaUrl
) {
    public IngresarFondosCommand {
        // Normalizar evidenciaUrl: si es null o vacío, establecer null
        if (evidenciaUrl != null && evidenciaUrl.isBlank()) {
            evidenciaUrl = null;
        }
    }
}
