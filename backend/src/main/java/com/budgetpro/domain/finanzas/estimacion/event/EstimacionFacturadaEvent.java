package com.budgetpro.domain.finanzas.estimacion.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class EstimacionFacturadaEvent {

    private final UUID estimacionId;
    private final UUID proyectoId;
    private final LocalDateTime facturadoAt;
    private final LocalDateTime occurredOn;

    public EstimacionFacturadaEvent(UUID estimacionId, UUID proyectoId, LocalDateTime facturadoAt) {
        this.estimacionId = estimacionId;
        this.proyectoId = proyectoId;
        this.facturadoAt = facturadoAt;
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getEstimacionId() {
        return estimacionId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDateTime getFacturadoAt() {
        return facturadoAt;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
