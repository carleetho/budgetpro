package com.budgetpro.domain.finanzas.estimacion.event;

import java.time.LocalDateTime;
import java.util.UUID;

public class EstimacionAnuladaEvent {

    private final UUID estimacionId;
    private final UUID proyectoId;
    private final String motivoAnulacion; // Optional, can be null
    private final LocalDateTime anuladoAt;
    private final LocalDateTime occurredOn;

    public EstimacionAnuladaEvent(UUID estimacionId, UUID proyectoId, String motivoAnulacion, LocalDateTime anuladoAt) {
        this.estimacionId = estimacionId;
        this.proyectoId = proyectoId;
        this.motivoAnulacion = motivoAnulacion;
        this.anuladoAt = anuladoAt;
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getEstimacionId() {
        return estimacionId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public LocalDateTime getAnuladoAt() {
        return anuladoAt;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
