package com.budgetpro.infrastructure.rest.presupuesto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de request REST para crear un presupuesto.
 */
public record CrearPresupuestoRequest(
        @NotNull(message = "El ID del proyecto es obligatorio")
        UUID proyectoId,
        
        @NotBlank(message = "El nombre del presupuesto es obligatorio")
        String nombre
) {
}
