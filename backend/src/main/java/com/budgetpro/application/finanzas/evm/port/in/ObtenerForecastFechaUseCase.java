package com.budgetpro.application.finanzas.evm.port.in;

import java.util.UUID;

/**
 * Puerto de entrada para obtener la fecha de finalización proyectada de un proyecto (REQ-63, UC-E05).
 */
public interface ObtenerForecastFechaUseCase {

    /**
     * Obtiene la fecha de finalización proyectada basada en SPI y cronograma.
     *
     * <p>Si no hay datos de series temporales o {@code spiAcumulado == 0}, se aplica fallback
     * usando {@code fechaFinPlanificada} del cronograma (si existe).
     *
     * @param proyectoId identificador UUID del proyecto
     * @return {@link ForecastResult} con la proyección o valores de fallback
     * @throws ProyectoNotFoundException si el proyecto no existe (HTTP 404)
     */
    ForecastResult obtener(UUID proyectoId);
}
