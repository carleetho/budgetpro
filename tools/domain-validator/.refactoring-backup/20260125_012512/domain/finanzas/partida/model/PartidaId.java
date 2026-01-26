package com.budgetpro.domain.finanzas.partida.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una Partida.
 */
public final class PartidaId {

    private final UUID value;

    private PartidaId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la partida no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo PartidaId.
     */
    public static PartidaId nuevo() {
        return new PartidaId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un PartidaId desde un UUID existente.
     */
    public static PartidaId from(UUID uuid) {
        return new PartidaId(uuid);
    }

    /**
     * Factory method para crear un PartidaId desde un String UUID.
     */
    public static PartidaId from(String uuidString) {
        return new PartidaId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartidaId that = (PartidaId) o;
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
