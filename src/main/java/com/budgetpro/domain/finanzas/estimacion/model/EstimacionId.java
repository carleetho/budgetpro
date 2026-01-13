package com.budgetpro.domain.finanzas.estimacion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una Estimacion.
 */
public final class EstimacionId {

    private final UUID value;

    private EstimacionId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la estimación no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo EstimacionId.
     */
    public static EstimacionId nuevo() {
        return new EstimacionId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un EstimacionId desde un UUID existente.
     */
    public static EstimacionId of(UUID uuid) {
        return new EstimacionId(uuid);
    }

    /**
     * Factory method para crear un EstimacionId desde un String UUID.
     */
    public static EstimacionId from(String uuidString) {
        return new EstimacionId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstimacionId that = (EstimacionId) o;
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
