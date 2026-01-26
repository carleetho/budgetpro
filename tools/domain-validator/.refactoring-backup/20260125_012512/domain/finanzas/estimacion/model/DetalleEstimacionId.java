package com.budgetpro.domain.finanzas.estimacion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un DetalleEstimacion.
 */
public final class DetalleEstimacionId {

    private final UUID value;

    private DetalleEstimacionId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del detalle de estimación no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo DetalleEstimacionId.
     */
    public static DetalleEstimacionId nuevo() {
        return new DetalleEstimacionId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un DetalleEstimacionId desde un UUID existente.
     */
    public static DetalleEstimacionId of(UUID uuid) {
        return new DetalleEstimacionId(uuid);
    }

    /**
     * Factory method para crear un DetalleEstimacionId desde un String UUID.
     */
    public static DetalleEstimacionId from(String uuidString) {
        return new DetalleEstimacionId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleEstimacionId that = (DetalleEstimacionId) o;
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
