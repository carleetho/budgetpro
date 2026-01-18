package com.budgetpro.application.compra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de comando para registrar una nueva compra.
 */
public record RegistrarCompraCommand(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,
        
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,
        
        @NotBlank(message = "El proveedor es obligatorio")
        String proveedor,
        
        @NotNull(message = "La lista de detalles es obligatoria")
        @Valid
        List<CompraDetalleCommand> detalles
) {
}
