package com.budgetpro.application.proyecto.dto;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de un proyecto.
 * 
 * Proyección de lectura para CQRS-Lite. NO contiene lógica de negocio.
 * 
 * Se usa exclusivamente para consultas READ (no para escrituras).
 */
public record ProyectoResponse(
        UUID id,
        String nombre,
        String estado
) {
}
