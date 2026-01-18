package com.budgetpro.application.partida.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para Partida.
 */
public record PartidaResponse(
        UUID id,
        UUID presupuestoId,
        UUID padreId, // null si es partida raíz
        String item,
        String descripcion,
        String unidad,
        BigDecimal metrado,
        Integer nivel,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
