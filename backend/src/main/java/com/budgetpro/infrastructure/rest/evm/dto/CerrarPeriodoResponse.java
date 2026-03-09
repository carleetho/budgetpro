package com.budgetpro.infrastructure.rest.evm.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de respuesta para cerrar un período de valuación (REQ-64).
 *
 * <p>periodoId coincide con el de ValuacionCerradaEvent (ej. "PER-2025-01-31") para traza de eventos.
 */
public record CerrarPeriodoResponse(
        UUID proyectoId,
        String periodoId,
        LocalDate fechaCorte,
        String status) {
}
