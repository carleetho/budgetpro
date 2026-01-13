package com.budgetpro.application.compra.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para un detalle de compra.
 */
public record CompraDetalleResponse(
        UUID id,
        UUID recursoId,
        UUID partidaId,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
