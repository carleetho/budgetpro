package com.budgetpro.application.finanzas.evm.port.in;

import java.time.LocalDate;
import java.util.UUID;

public interface ObtenerSCurveUseCase {

    /**
     * Obtiene la serie de datos para la curva S de un proyecto.
     *
     * @param proyectoId identificador UUID del proyecto
     * @param startDate fecha de inicio inclusiva (nullable: {@code null} = desde el inicio del proyecto)
     * @param endDate fecha de fin inclusiva (nullable: {@code null} = hasta la fecha actual)
     * @return {@link SCurveResult} con la curva S; lista {@code dataPoints} vacía si no hay datos
     * @throws ProyectoNotFoundException si el proyecto no existe (HTTP 404)
     */
    SCurveResult obtener(UUID proyectoId, LocalDate startDate, LocalDate endDate);
}
