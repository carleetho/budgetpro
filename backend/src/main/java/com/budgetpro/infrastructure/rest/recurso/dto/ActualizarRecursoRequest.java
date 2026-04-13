package com.budgetpro.infrastructure.rest.recurso.dto;

import com.budgetpro.application.recurso.dto.ActualizarRecursoCommand;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record ActualizarRecursoRequest(
        @Size(max = 200) String nombre,
        @Size(max = 20) String unidadBase,
        Map<String, Object> atributos,
        String estado
) {
    public ActualizarRecursoCommand toCommand(UUID id) {
        return new ActualizarRecursoCommand(id, nombre, unidadBase, atributos, estado);
    }
}

