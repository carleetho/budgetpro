package com.budgetpro.infrastructure.rest.compra.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de request REST para recibir una orden de compra.
 * 
 * Representa el payload de la API para registrar una recepción de productos
 * con su guía de remisión y detalles de recepción.
 */
public record RecibirOrdenCompraRequest(
        @NotNull(message = "La fecha de recepción es obligatoria")
        LocalDate fechaRecepcion,
        
        @NotBlank(message = "La guía de remisión es obligatoria")
        String guiaRemision,
        
        @NotNull(message = "La lista de detalles es obligatoria")
        @NotEmpty(message = "La recepción debe tener al menos un detalle")
        @Valid
        List<RecepcionDetalleRequest> detalles
) {
}
