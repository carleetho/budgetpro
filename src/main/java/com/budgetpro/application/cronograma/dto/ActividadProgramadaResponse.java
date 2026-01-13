package com.budgetpro.application.cronograma.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para ActividadProgramada.
 */
public record ActividadProgramadaResponse(
        UUID id,
        UUID partidaId,
        UUID programaObraId,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer duracionDias,
        List<UUID> predecesoras,
        Integer version
) {
}
