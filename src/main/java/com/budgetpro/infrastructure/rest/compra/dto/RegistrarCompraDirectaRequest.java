package com.budgetpro.infrastructure.rest.compra.dto;

import com.budgetpro.application.compra.dto.RegistrarCompraDirectaCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO de Request para registrar una compra directa.
 * 
 * Representa el JSON recibido del cliente REST.
 * Este DTO pertenece a la capa de infraestructura y se adapta al Command de la capa de aplicación.
 */
public record RegistrarCompraDirectaRequest(
        @NotNull(message = "El ID del proyecto es obligatorio")
        @JsonProperty("proyectoId")
        UUID proyectoId,

        @NotNull(message = "El ID del presupuesto es obligatorio")
        @JsonProperty("presupuestoId")
        UUID presupuestoId,

        @NotEmpty(message = "La compra debe tener al menos un detalle")
        @Valid
        @JsonProperty("detalles")
        List<DetalleCompraRequest> detalles
) {
    /**
     * DTO anidado que representa un detalle de compra en el request.
     */
    public record DetalleCompraRequest(
            @NotNull(message = "El ID del recurso es obligatorio")
            @JsonProperty("recursoId")
            UUID recursoId,

            @NotNull(message = "La cantidad es obligatoria")
            @DecimalMin(value = "0.000001", message = "La cantidad debe ser mayor que cero")
            @JsonProperty("cantidad")
            BigDecimal cantidad,

            @NotNull(message = "El precio unitario es obligatorio")
            @DecimalMin(value = "0.0001", message = "El precio unitario debe ser mayor o igual que cero")
            @JsonProperty("precioUnitario")
            BigDecimal precioUnitario
    ) {
    }

    /**
     * Convierte este Request DTO al Command de la capa de aplicación.
     * 
     * @return El comando correspondiente
     */
    public RegistrarCompraDirectaCommand toCommand() {
        List<RegistrarCompraDirectaCommand.DetalleCompraCommand> detallesCommand = detalles.stream()
                .map(detalle -> new RegistrarCompraDirectaCommand.DetalleCompraCommand(
                        detalle.recursoId(),
                        detalle.cantidad(),
                        detalle.precioUnitario()
                ))
                .collect(Collectors.toList());

        return new RegistrarCompraDirectaCommand(
                proyectoId,
                presupuestoId,
                detallesCommand
        );
    }
}
