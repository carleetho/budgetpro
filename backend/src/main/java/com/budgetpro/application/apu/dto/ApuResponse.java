package com.budgetpro.application.apu.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para APU.
 */
public record ApuResponse(
        UUID id,
        UUID partidaId,
        BigDecimal rendimiento,
        String unidad,
        BigDecimal costoTotal,
        Integer version,
        List<ApuInsumoResponse> insumos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
