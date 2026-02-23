package com.budgetpro.infrastructure.rest.compra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de request REST para un detalle de recepción.
 * 
 * Representa la información de recepción de un recurso específico
 * asociado a un detalle de orden de compra.
 */
public record RecepcionDetalleRequest(
        @NotNull(message = "El ID del detalle de orden es obligatorio")
        UUID detalleOrdenId,
        
        @NotNull(message = "La cantidad recibida es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad recibida debe ser mayor que cero")
        BigDecimal cantidadRecibida,
        
        @NotNull(message = "El ID del almacén es obligatorio")
        UUID almacenId
) {
}
