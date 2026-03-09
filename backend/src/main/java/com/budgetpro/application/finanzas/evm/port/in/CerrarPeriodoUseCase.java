package com.budgetpro.application.finanzas.evm.port.in;

import com.budgetpro.application.finanzas.evm.exception.PeriodoFechaInvalidaException;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Puerto de entrada para cerrar un período de valuación en un proyecto (REQ-64, Invariante E-04).
 *
 * <p>Valida que la fecha de corte esté alineada con la frecuencia configurada del proyecto
 * antes de publicar {@link com.budgetpro.application.finanzas.evm.event.ValuacionCerradaEvent}.
 */
public interface CerrarPeriodoUseCase {

    /**
     * Cierra el período de valuación para el proyecto en la fecha de corte dada.
     *
     * @param proyectoId identificador UUID del proyecto
     * @param fechaCorte  fecha de cierre del período
     * @return identificador del período cerrado (ej. "PER-2025-01-31"), usado en ValuacionCerradaEvent
     * @throws ProyectoNotFoundException   si el proyecto no existe (HTTP 404)
     * @throws PeriodoFechaInvalidaException si fechaCorte no está alineada con la frecuencia
     *                                       del proyecto (HTTP 422)
     */
    String cerrar(UUID proyectoId, LocalDate fechaCorte);
}
