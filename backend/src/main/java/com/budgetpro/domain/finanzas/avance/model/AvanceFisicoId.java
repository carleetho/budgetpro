package com.budgetpro.domain.finanzas.avance.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un AvanceFisico.
 */
public final class AvanceFisicoId {

    private final UUID value;

    private AvanceFisicoId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del avance físico no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo AvanceFisicoId.
     */
    public static AvanceFisicoId nuevo() {
        return new AvanceFisicoId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un AvanceFisicoId desde un UUID existente.
     */
    public static AvanceFisicoId of(UUID uuid) {
        return new AvanceFisicoId(uuid);
    }

    /**
     * Factory method para crear un AvanceFisicoId desde un String UUID.
     */
    public static AvanceFisicoId from(String uuidString) {
        return new AvanceFisicoId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvanceFisicoId that = (AvanceFisicoId) o;
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
