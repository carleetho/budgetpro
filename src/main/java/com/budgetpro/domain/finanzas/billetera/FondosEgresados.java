package com.budgetpro.domain.finanzas.billetera;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio que se dispara cuando se egresan fondos de una Billetera.
 */
public final class FondosEgresados implements DomainEvent {

    private final UUID eventId;
    private final LocalDateTime occurredAt;
    private final BilleteraId billeteraId;
    private final Monto monto;
    private final String referencia;
    private final String evidenciaUrl;

    public FondosEgresados(BilleteraId billeteraId, Monto monto, String referencia, String evidenciaUrl) {
        this.eventId = UUID.randomUUID();
        this.occurredAt = LocalDateTime.now();
        this.billeteraId = Objects.requireNonNull(billeteraId, "El billeteraId no puede ser nulo");
        this.monto = Objects.requireNonNull(monto, "El monto no puede ser nulo");
        this.referencia = Objects.requireNonNull(referencia, "La referencia no puede ser nula");
        this.evidenciaUrl = evidenciaUrl;
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

    public Monto getMonto() {
        return monto;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FondosEgresados that = (FondosEgresados) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("FondosEgresados{eventId=%s, billeteraId=%s, monto=%s, referencia='%s', occurredAt=%s}",
                           eventId, billeteraId, monto, referencia, occurredAt);
    }
}
