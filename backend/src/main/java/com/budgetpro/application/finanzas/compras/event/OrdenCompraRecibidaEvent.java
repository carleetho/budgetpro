package com.budgetpro.application.finanzas.compras.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * DECLARED FOR FUTURE USE - listener NOT implemented in this PR.
 */
public record OrdenCompraRecibidaEvent(UUID proyectoId, LocalDate fechaCorte, BigDecimal acNuevo) {
    public OrdenCompraRecibidaEvent {
        Objects.requireNonNull(proyectoId, "proyectoId is required");
        Objects.requireNonNull(fechaCorte, "fechaCorte is required");
        Objects.requireNonNull(acNuevo, "acNuevo is required");
    }
}
