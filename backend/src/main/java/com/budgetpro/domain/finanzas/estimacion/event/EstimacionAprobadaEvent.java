package com.budgetpro.domain.finanzas.estimacion.event;

import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.MontoEstimado;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EstimacionAprobadaEvent {

    private final UUID estimacionId;
    private final UUID proyectoId;
    private final MontoEstimado montoTotalEstimado;
    private final List<DetalleEstimacion> items;
    private final LocalDateTime aprobadoAt;
    private final LocalDateTime occurredOn;

    public EstimacionAprobadaEvent(UUID estimacionId, UUID proyectoId, MontoEstimado montoTotalEstimado,
            List<DetalleEstimacion> items, LocalDateTime aprobadoAt) {
        this.estimacionId = estimacionId;
        this.proyectoId = proyectoId;
        this.montoTotalEstimado = montoTotalEstimado;
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.aprobadoAt = aprobadoAt;
        this.occurredOn = LocalDateTime.now();
    }

    public UUID getEstimacionId() {
        return estimacionId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public MontoEstimado getMontoTotalEstimado() {
        return montoTotalEstimado;
    }

    public List<DetalleEstimacion> getItems() {
        return items;
    }

    public LocalDateTime getAprobadoAt() {
        return aprobadoAt;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
