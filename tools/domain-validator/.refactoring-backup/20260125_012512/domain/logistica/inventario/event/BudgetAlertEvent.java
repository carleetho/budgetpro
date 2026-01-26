package com.budgetpro.domain.logistica.inventario.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetAlertEvent(
        UUID proyectoId,
        UUID partidaId,
        double porcentajeEjecucion,
        BudgetAlertSeverity severity,
        LocalDateTime timestamp) {
    public enum BudgetAlertSeverity {
        WARNING, // > 80%
        CRITICAL // > 100%
    }
}
