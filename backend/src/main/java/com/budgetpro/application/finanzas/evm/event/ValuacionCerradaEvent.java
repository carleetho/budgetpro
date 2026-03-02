package com.budgetpro.application.finanzas.evm.event;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record ValuacionCerradaEvent(UUID proyectoId, String periodoId, LocalDate fechaCorte) {
    public ValuacionCerradaEvent {
        Objects.requireNonNull(proyectoId, "proyectoId is required");
        Objects.requireNonNull(periodoId, "periodoId is required");
        Objects.requireNonNull(fechaCorte, "fechaCorte is required");
    }
}
