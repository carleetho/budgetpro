package com.budgetpro.infrastructure.rest.compra.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request REST para crear o actualizar una orden de compra.
 */
@Schema(description = "Request para crear o actualizar una orden de compra")
public record OrdenCompraRequest(
        @Schema(description = "ID único del proyecto", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,
        
        @Schema(description = "ID único del proveedor (debe estar ACTIVO)", required = true, example = "660e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El ID del proveedor es obligatorio")
        UUID proveedorId,
        
        @Schema(description = "Fecha de la orden de compra", required = true, example = "2024-02-15")
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,
        
        @Schema(description = "Condiciones de pago (opcional)", example = "30 días crédito")
        String condicionesPago,
        
        @Schema(description = "Observaciones adicionales (opcional)", example = "Entrega urgente requerida")
        String observaciones,
        
        @Schema(description = "Lista de detalles de la orden de compra (mínimo 1)", required = true)
        @NotNull(message = "La lista de detalles es obligatoria")
        @NotEmpty(message = "La orden debe tener al menos un detalle")
        @Valid
        List<DetalleOrdenCompraRequest> detalles
) {
}
