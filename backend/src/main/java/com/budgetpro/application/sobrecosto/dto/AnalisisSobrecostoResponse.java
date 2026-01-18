package com.budgetpro.application.sobrecosto.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para AnalisisSobrecosto.
 */
public record AnalisisSobrecostoResponse(
        UUID id,
        UUID presupuestoId,
        BigDecimal porcentajeIndirectosOficinaCentral,
        BigDecimal porcentajeIndirectosOficinaCampo,
        BigDecimal porcentajeIndirectosTotal,
        BigDecimal porcentajeFinanciamiento,
        Boolean financiamientoCalculado,
        BigDecimal porcentajeUtilidad,
        BigDecimal porcentajeFianzas,
        BigDecimal porcentajeImpuestosReflejables,
        BigDecimal porcentajeCargosAdicionalesTotal,
        Integer version
) {
}
