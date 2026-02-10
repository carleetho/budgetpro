package com.budgetpro.infrastructure.rest.cronograma.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request para programar una actividad.
 */
public record ProgramarActividadRequest(
        @NotNull(message = "El partidaId es obligatorio")
        UUID partidaId,
        
        // REGLA-089
        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,
        
        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,
        
        List<UUID> predecesoras // IDs de actividades predecesoras (opcional)
) {
}
