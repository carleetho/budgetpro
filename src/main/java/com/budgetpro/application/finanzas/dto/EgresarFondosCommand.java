package com.budgetpro.application.finanzas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa el comando para egresar fondos de una billetera.
 * 
 * Usado como entrada del caso de uso EgresarFondosUseCase.
 * 
 * REGLA: El monto a egresar no puede exceder el saldo actual (validado en el dominio).
 */
public record EgresarFondosCommand(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0001", message = "El monto debe ser mayor que cero", inclusive = false)
        BigDecimal monto,

        @NotBlank(message = "La referencia es obligatoria")
        String referencia,

        String evidenciaUrl
) {
    public EgresarFondosCommand {
        // Normalizar evidenciaUrl: si es null o vacío, establecer null
        if (evidenciaUrl != null && evidenciaUrl.isBlank()) {
            evidenciaUrl = null;
        }
    }
}
