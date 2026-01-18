package com.budgetpro.application.proyecto.dto;

import com.budgetpro.domain.proyecto.model.EstadoProyecto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para Proyecto.
 */
public record ProyectoResponse(
        UUID id,
        String nombre,
        String ubicacion,
        EstadoProyecto estado,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
