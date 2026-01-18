package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para DetalleEstimacion.
 */
public record DetalleEstimacionResponse(
        UUID id,
        UUID partidaId,
        BigDecimal cantidadAvance,
        BigDecimal precioUnitario,
        BigDecimal importe,
        BigDecimal acumuladoAnterior
) {
}
