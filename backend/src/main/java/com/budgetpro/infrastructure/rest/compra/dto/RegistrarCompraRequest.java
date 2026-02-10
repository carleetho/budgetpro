package com.budgetpro.infrastructure.rest.compra.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request REST para registrar una compra.
 */
public record RegistrarCompraRequest(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,
        
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,
        
        // REGLA-092
        @NotBlank(message = "El proveedor es obligatorio")
        String proveedor,
        
        @NotNull(message = "La lista de detalles es obligatoria")
        @Valid
        List<CompraDetalleRequest> detalles
) {
}
