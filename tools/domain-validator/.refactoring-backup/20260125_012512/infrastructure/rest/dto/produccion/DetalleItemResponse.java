package com.budgetpro.infrastructure.rest.dto.produccion;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de detalle para respuesta de reporte.
 */
public record DetalleItemResponse(
        UUID partidaId,
        String partidaDescripcion,
        BigDecimal cantidad
) {
}
