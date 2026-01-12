package com.budgetpro.infrastructure.rest.partida.dto;

import com.budgetpro.application.partida.dto.CrearPartidaCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de Request para crear una nueva partida.
 * 
 * Representa el JSON recibido del cliente REST.
 * Este DTO pertenece a la capa de infraestructura y se adapta al Command de la capa de aplicación.
 */
public record CrearPartidaRequest(
        @NotBlank(message = "El código de la partida es obligatorio")
        @JsonProperty("codigo")
        String codigo,

        @NotBlank(message = "El nombre de la partida es obligatorio")
        @JsonProperty("nombre")
        String nombre,

        @NotBlank(message = "El tipo de la partida es obligatorio")
        @JsonProperty("tipo")
        String tipo,

        @NotNull(message = "El monto presupuestado es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El monto presupuestado no puede ser negativo")
        @JsonProperty("montoPresupuestado")
        BigDecimal montoPresupuestado
) {
    /**
     * Convierte este Request DTO al Command de la capa de aplicación.
     * 
     * @param presupuestoId El ID del presupuesto (obtenido de la URL path)
     * @return El comando correspondiente
     */
    public CrearPartidaCommand toCommand(UUID presupuestoId) {
        return new CrearPartidaCommand(
            presupuestoId,
            codigo,
            nombre,
            tipo,
            montoPresupuestado
        );
    }
}
