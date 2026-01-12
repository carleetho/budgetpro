package com.budgetpro.application.recurso.dto;

import com.budgetpro.domain.recurso.model.EstadoRecurso;
import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de búsqueda de un recurso.
 * 
 * Proyección de lectura para CQRS-Lite. NO contiene lógica de negocio.
 * 
 * Se usa exclusivamente para consultas READ (autocomplete).
 */
public record RecursoSearchResponse(
        UUID id,
        String nombre,
        TipoRecurso tipo,
        String unidadBase,
        EstadoRecurso estado
) {
}
