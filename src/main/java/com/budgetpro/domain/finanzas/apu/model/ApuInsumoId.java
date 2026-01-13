package com.budgetpro.domain.finanzas.apu.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un ApuInsumo.
 */
public final class ApuInsumoId {

    private final UUID value;

    private ApuInsumoId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del ApuInsumo no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ApuInsumoId.
     */
    public static ApuInsumoId nuevo() {
        return new ApuInsumoId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ApuInsumoId desde un UUID existente.
     */
    public static ApuInsumoId from(UUID uuid) {
        return new ApuInsumoId(uuid);
    }

    /**
     * Factory method para crear un ApuInsumoId desde un String UUID.
     */
    public static ApuInsumoId from(String uuidString) {
        return new ApuInsumoId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApuInsumoId that = (ApuInsumoId) o;
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
