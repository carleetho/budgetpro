package com.budgetpro.domain.logistica.inventario.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaterialConsumed(
        UUID proyectoId,
        UUID partidaId,
        String recursoExternalId,
        BigDecimal cantidad,
        BigDecimal costoTotal, // Actual Cost (AC)
        LocalDateTime fechaConsumo,
        String referencia) {
}
