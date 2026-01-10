package com.budgetpro.domain.finanzas.billetera;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que se dispara cuando se crea una nueva Billetera.
 */
public final class BilleteraCreada implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final BilleteraId billeteraId;
    private final UUID proyectoId;

    public BilleteraCreada(BilleteraId billeteraId, UUID proyectoId) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.billeteraId = Objects.requireNonNull(billeteraId, "El billeteraId no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOcurredAt() {
        return occurredAt;
    }

    public BilleteraId getBilleteraId() {
        return billeteraId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BilleteraCreada that = (BilleteraCreada) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("BilleteraCreada{eventId=%s, billeteraId=%s, proyectoId=%s, occurredAt=%s}",
                           eventId, billeteraId, proyectoId, occurredAt);
    }
}
