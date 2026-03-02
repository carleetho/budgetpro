package com.budgetpro.application.finanzas.avance.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * DECLARED FOR FUTURE USE - listener NOT implemented in this PR.
 */
public record AvanceFisicoRegistradoEvent(UUID proyectoId, LocalDate fechaCorte, BigDecimal evNuevo) {
    public AvanceFisicoRegistradoEvent {
        Objects.requireNonNull(proyectoId, "proyectoId is required");
        Objects.requireNonNull(fechaCorte, "fechaCorte is required");
        Objects.requireNonNull(evNuevo, "evNuevo is required");
    }
}
