package com.budgetpro.application.presupuesto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de comando para crear un nuevo presupuesto.
 */
public record CrearPresupuestoCommand(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,
        
        @NotBlank(message = "El nombre del presupuesto es obligatorio")
        String nombre
) {
}
