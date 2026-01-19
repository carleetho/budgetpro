package com.budgetpro.application.compra.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para un detalle de compra.
 */
public record CompraDetalleResponse(
        UUID id,
        String recursoExternalId,
        String recursoNombre,
        UUID partidaId,
        com.budgetpro.domain.logistica.compra.model.NaturalezaGasto naturalezaGasto,
        com.budgetpro.domain.logistica.compra.model.RelacionContractual relacionContractual,
        com.budgetpro.domain.logistica.compra.model.RubroInsumo rubroInsumo,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
