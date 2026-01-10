package com.budgetpro.application.recurso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO (Data Transfer Object) que representa el comando para crear un nuevo recurso.
 * 
 * Usado como entrada del caso de uso CrearRecursoUseCase.
 */
public record CrearRecursoCommand(
        @NotBlank(message = "El nombre del recurso es obligatorio")
        String nombre,

        @NotBlank(message = "El tipo del recurso es obligatorio")
        String tipo,

        @NotBlank(message = "La unidad base del recurso es obligatoria")
        String unidadBase,

        Map<String, Object> atributos,

        boolean esProvisional
) {
    public CrearRecursoCommand {
        // Validación de atributos: si es nulo, inicializar con HashMap vacío
        if (atributos == null) {
            atributos = new HashMap<>();
        } else {
            // Crear una copia defensiva para evitar mutaciones externas
            atributos = new HashMap<>(atributos);
        }
    }
}
