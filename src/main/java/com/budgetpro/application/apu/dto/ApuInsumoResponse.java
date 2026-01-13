package com.budgetpro.application.apu.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para un insumo del APU.
 */
public record ApuInsumoResponse(
        UUID id,
        UUID recursoId,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
