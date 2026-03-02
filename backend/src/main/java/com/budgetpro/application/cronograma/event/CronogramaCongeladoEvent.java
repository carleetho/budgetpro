package com.budgetpro.application.cronograma.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * DECLARED FOR FUTURE USE - listener NOT implemented in this PR.
 */
public record CronogramaCongeladoEvent(UUID proyectoId, LocalDate fechaCorte, BigDecimal pvBaseline) {
    public CronogramaCongeladoEvent {
        Objects.requireNonNull(proyectoId, "proyectoId is required");
        Objects.requireNonNull(fechaCorte, "fechaCorte is required");
        Objects.requireNonNull(pvBaseline, "pvBaseline is required");
    }
}
