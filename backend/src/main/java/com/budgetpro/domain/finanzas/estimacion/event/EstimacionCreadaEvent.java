package com.budgetpro.domain.finanzas.estimacion.event;

import com.budgetpro.domain.finanzas.estimacion.model.PeriodoEstimacion;

import java.time.LocalDateTime;
import java.util.UUID;

public class EstimacionCreadaEvent {

    private final UUID estimacionId;
    private final UUID proyectoId;
    private final PeriodoEstimacion periodo;
    private final LocalDateTime occurredOn;

    public EstimacionCreadaEvent(UUID estimacionId, UUID proyectoId, PeriodoEstimacion periodo) {
        this.estimacionId = estimacionId;
        this.proyectoId = proyectoId;
        this.periodo = periodo;
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getEstimacionId() {
        return estimacionId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public PeriodoEstimacion getPeriodo() {
        return periodo;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
