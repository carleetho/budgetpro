package com.budgetpro.infrastructure.rest.finanzas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de Request para ingresar fondos a una billetera vía REST API.
 * 
 * Convierte el request HTTP (JSON) al Command del caso de uso.
 */
public record IngresarFondosRequest(
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0001", message = "El monto debe ser mayor que cero", inclusive = false)
        BigDecimal monto,

        @NotBlank(message = "La referencia es obligatoria")
        String referencia,

        String evidenciaUrl
) {
    /**
     * Convierte este Request DTO al Command del caso de uso.
     * 
     * @param proyectoId El ID del proyecto (viene del path variable)
     * @return El Command del caso de uso
     */
    public com.budgetpro.application.finanzas.dto.IngresarFondosCommand toCommand(UUID proyectoId) {
        return new com.budgetpro.application.finanzas.dto.IngresarFondosCommand(
            proyectoId,
            monto,
            referencia,
            evidenciaUrl
        );
    }
}
