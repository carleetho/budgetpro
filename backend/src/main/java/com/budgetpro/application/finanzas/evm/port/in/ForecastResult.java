package com.budgetpro.application.finanzas.evm.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resultado de aplicación para la consulta de fecha de finalización proyectada (REQ-63, UC-E05).
 *
 * <p>Registro interno de la capa de aplicación; no es el DTO HTTP.
 */
public record ForecastResult(
        UUID proyectoId,
        LocalDate fechaCorteBase,
        LocalDate forecastCompletionDate,
        LocalDate fechaFinPlanificada,
        int remainingDays,
        BigDecimal spiUsed,
        boolean forecastFallback) {
}
