package com.budgetpro.application.estimacion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de comando para generar una estimación.
 */
public record GenerarEstimacionCommand(
        UUID proyectoId,
        LocalDate fechaCorte,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        List<DetalleEstimacionItem> detalles, // Detalles por partida
        BigDecimal porcentajeAnticipo, // Porcentaje de anticipo (ej: 30%)
        BigDecimal porcentajeRetencionFondoGarantia // Porcentaje de retención (ej: 5%)
) {
    /**
     * DTO interno para representar un detalle de estimación.
     */
    public record DetalleEstimacionItem(
            UUID partidaId,
            java.math.BigDecimal cantidadAvance,
            java.math.BigDecimal precioUnitario
    ) {
    }
}
