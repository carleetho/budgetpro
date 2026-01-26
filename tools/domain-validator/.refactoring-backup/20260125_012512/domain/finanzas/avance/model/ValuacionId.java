package com.budgetpro.domain.finanzas.avance.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una Valuacion.
 */
public final class ValuacionId {

    private final UUID value;

    private ValuacionId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la valuación no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ValuacionId.
     */
    public static ValuacionId nuevo() {
        return new ValuacionId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ValuacionId desde un UUID existente.
     */
    public static ValuacionId of(UUID uuid) {
        return new ValuacionId(uuid);
    }

    /**
     * Factory method para crear un ValuacionId desde un String UUID.
     */
    public static ValuacionId from(String uuidString) {
        return new ValuacionId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValuacionId that = (ValuacionId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
