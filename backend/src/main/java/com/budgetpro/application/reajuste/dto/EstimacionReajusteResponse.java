package com.budgetpro.application.reajuste.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para una estimación de reajuste.
 */
public record EstimacionReajusteResponse(
    UUID id,
    UUID proyectoId,
    UUID presupuestoId,
    Integer numeroEstimacion,
    LocalDate fechaCorte,
    String indiceBaseCodigo,
    LocalDate indiceBaseFecha,
    String indiceActualCodigo,
    LocalDate indiceActualFecha,
    BigDecimal valorIndiceBase,
    BigDecimal valorIndiceActual,
    BigDecimal montoBase,
    BigDecimal montoReajustado,
    BigDecimal diferencial,
    BigDecimal porcentajeVariacion,
    String estado,
    String observaciones,
    List<DetalleReajustePartidaResponse> detalles
) {
    
    /**
     * DTO de respuesta para un detalle de reajuste por partida.
     */
    public record DetalleReajustePartidaResponse(
        UUID id,
        UUID partidaId,
        BigDecimal montoBase,
        BigDecimal montoReajustado,
        BigDecimal diferencial
    ) {}
}
