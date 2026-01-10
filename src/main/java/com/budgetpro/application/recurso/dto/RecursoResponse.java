package com.budgetpro.application.recurso.dto;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta al crear o consultar un recurso.
 * 
 * Usado como salida del caso de uso CrearRecursoUseCase y otras operaciones de lectura.
 * Contiene solo los campos necesarios para la capa de presentación.
 */
public record RecursoResponse(
        UUID id,
        String nombre,
        String tipo,
        String estado
) {
}
