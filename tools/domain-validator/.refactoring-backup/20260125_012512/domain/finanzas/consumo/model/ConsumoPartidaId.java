package com.budgetpro.domain.finanzas.consumo.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un ConsumoPartida.
 */
public final class ConsumoPartidaId {

    private final UUID value;

    private ConsumoPartidaId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del consumo no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ConsumoPartidaId.
     */
    public static ConsumoPartidaId nuevo() {
        return new ConsumoPartidaId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ConsumoPartidaId desde un UUID existente.
     */
    public static ConsumoPartidaId from(UUID uuid) {
        return new ConsumoPartidaId(uuid);
    }

    /**
     * Factory method para crear un ConsumoPartidaId desde un String UUID.
     */
    public static ConsumoPartidaId from(String uuidString) {
        return new ConsumoPartidaId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumoPartidaId that = (ConsumoPartidaId) o;
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
