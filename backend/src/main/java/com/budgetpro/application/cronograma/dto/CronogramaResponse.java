package com.budgetpro.application.cronograma.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para el cronograma completo (Gantt de datos).
 */
public record CronogramaResponse(
        UUID programaObraId,
        UUID proyectoId,
        LocalDate fechaInicio,
        LocalDate fechaFinEstimada,
        Integer duracionTotalDias,
        Integer duracionMeses, // Para cálculo de financiamiento
        List<ActividadProgramadaResponse> actividades,
        Integer version
) {
}
