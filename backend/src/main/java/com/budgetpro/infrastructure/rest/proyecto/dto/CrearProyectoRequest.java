package com.budgetpro.infrastructure.rest.proyecto.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de request REST para crear un proyecto.
 */
public record CrearProyectoRequest(
        // REGLA-097
        @NotBlank(message = "El nombre del proyecto es obligatorio")
        String nombre,
        
        String ubicacion
) {
}
