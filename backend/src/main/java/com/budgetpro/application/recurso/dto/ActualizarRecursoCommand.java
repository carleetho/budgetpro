package com.budgetpro.application.recurso.dto;

import java.util.Map;
import java.util.UUID;

public record ActualizarRecursoCommand(
        UUID id,
        String nombre,
        String unidadBase,
        Map<String, Object> atributos,
        String estado
) {
}

