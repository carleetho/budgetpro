package com.budgetpro.domain.finanzas.sobrecosto.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un AnalisisSobrecosto.
 */
public final class AnalisisSobrecostoId {

    private final UUID value;

    private AnalisisSobrecostoId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del análisis de sobrecosto no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo AnalisisSobrecostoId.
     */
    public static AnalisisSobrecostoId nuevo() {
        return new AnalisisSobrecostoId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un AnalisisSobrecostoId desde un UUID existente.
     */
    public static AnalisisSobrecostoId of(UUID uuid) {
        return new AnalisisSobrecostoId(uuid);
    }

    /**
     * Factory method para crear un AnalisisSobrecostoId desde un String UUID.
     */
    public static AnalisisSobrecostoId from(String uuidString) {
        return new AnalisisSobrecostoId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalisisSobrecostoId that = (AnalisisSobrecostoId) o;
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
