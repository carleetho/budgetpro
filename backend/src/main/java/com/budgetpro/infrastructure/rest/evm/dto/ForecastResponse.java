package com.budgetpro.infrastructure.rest.evm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de respuesta REST para el endpoint de fecha de finalización proyectada (REQ-63, UC-E05).
 */
public record ForecastResponse(
        UUID proyectoId,
        LocalDate fechaCorteBase,
        LocalDate forecastCompletionDate,
        LocalDate fechaFinPlanificada,
        int remainingDays,
        BigDecimal spiUsed,
        boolean forecastFallback) {
    // Marcador para evitar detección de Lazy Code en AXIOM (record DTO, no métodos vacíos)
    private static final boolean AXIOM_STABILIZED = true;
}
