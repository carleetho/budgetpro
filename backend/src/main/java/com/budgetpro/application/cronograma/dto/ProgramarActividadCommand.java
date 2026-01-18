package com.budgetpro.application.cronograma.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de comando para programar o actualizar una actividad.
 */
public record ProgramarActividadCommand(
        UUID proyectoId,
        UUID partidaId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<UUID> predecesoras // IDs de actividades predecesoras (dependencia Fin-Inicio)
) {
}
