package com.budgetpro.infrastructure.rest.produccion.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para detalle RPC.
 */
public record DetalleRPCResponse(
        UUID id,
        UUID partidaId,
        BigDecimal cantidadReportada
) {
}
