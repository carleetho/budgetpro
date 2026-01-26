package com.budgetpro.infrastructure.rest.recurso.dto;

import com.budgetpro.application.recurso.dto.CrearRecursoCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO de Request para crear un nuevo recurso.
 * 
 * Representa el JSON recibido del cliente REST.
 * Este DTO pertenece a la capa de infraestructura y se adapta al Command de la capa de aplicación.
 */
public record CrearRecursoRequest(
        @NotBlank(message = "El nombre del recurso es obligatorio")
        @JsonProperty("nombre")
        String nombre,

        @NotBlank(message = "El tipo del recurso es obligatorio")
        @JsonProperty("tipo")
        String tipo,

        @NotBlank(message = "La unidad base del recurso es obligatoria")
        @JsonProperty("unidadBase")
        String unidadBase,

        @JsonProperty("atributos")
        Map<String, Object> atributos,

        @JsonProperty("esProvisional")
        boolean esProvisional
) {
    public CrearRecursoRequest {
        // Inicializar atributos si es nulo
        if (atributos == null) {
            atributos = new HashMap<>();
        }
    }

    /**
     * Convierte este Request DTO al Command de la capa de aplicación.
     * 
     * @return El comando correspondiente
     */
    public CrearRecursoCommand toCommand() {
        return new CrearRecursoCommand(
            nombre,
            tipo,
            unidadBase,
            atributos,
            esProvisional
        );
    }
}
