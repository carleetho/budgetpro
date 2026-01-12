package com.budgetpro.application.compra.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa el comando para registrar una compra directa.
 * 
 * Usado como entrada del caso de uso RegistrarCompraDirectaUseCase.
 */
public record RegistrarCompraDirectaCommand(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,

        @NotNull(message = "El ID del presupuesto es obligatorio")
        UUID presupuestoId,

        @NotEmpty(message = "La compra debe tener al menos un detalle")
        @Valid
        List<DetalleCompraCommand> detalles
) {
    /**
     * DTO anidado que representa un detalle de compra en el comando.
     */
    public record DetalleCompraCommand(
            @NotNull(message = "El ID del recurso es obligatorio")
            UUID recursoId,

            @NotNull(message = "La cantidad es obligatoria")
            java.math.BigDecimal cantidad,

            @NotNull(message = "El precio unitario es obligatorio")
            java.math.BigDecimal precioUnitario
    ) {
    }
}
