package com.budgetpro.infrastructure.rest.compra.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta REST para un detalle de orden de compra.
 */
public record DetalleOrdenCompraResponse(
        UUID partidaId,
        String descripcion,
        BigDecimal cantidad,
        String unidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
    /**
     * Constructor compacto con validación.
     */
    public DetalleOrdenCompraResponse {
        if (partidaId == null) {
            throw new IllegalArgumentException("partidaId no puede ser null");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("cantidad debe ser mayor que cero");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("precioUnitario no puede ser negativo");
        }
    }
}
