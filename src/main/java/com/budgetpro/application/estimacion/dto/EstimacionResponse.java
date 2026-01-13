package com.budgetpro.application.estimacion.dto;

import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para Estimacion.
 */
public record EstimacionResponse(
        UUID id,
        UUID proyectoId,
        Integer numeroEstimacion,
        LocalDate fechaCorte,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        BigDecimal montoBruto,
        BigDecimal amortizacionAnticipo,
        BigDecimal retencionFondoGarantia,
        BigDecimal montoNetoPagar,
        EstadoEstimacion estado,
        List<DetalleEstimacionResponse> detalles,
        Integer version
) {
}
