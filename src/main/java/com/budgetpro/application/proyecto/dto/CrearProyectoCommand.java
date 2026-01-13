package com.budgetpro.application.proyecto.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de comando para crear un nuevo proyecto.
 */
public record CrearProyectoCommand(
        @NotBlank(message = "El nombre del proyecto es obligatorio")
        String nombre,
        
        String ubicacion
) {
}
