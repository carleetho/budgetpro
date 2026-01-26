package com.budgetpro.domain.finanzas.apu.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un APU.
 */
public final class ApuId {

    private final UUID value;

    private ApuId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del APU no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ApuId.
     */
    public static ApuId nuevo() {
        return new ApuId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ApuId desde un UUID existente.
     */
    public static ApuId from(UUID uuid) {
        return new ApuId(uuid);
    }

    /**
     * Factory method para crear un ApuId desde un String UUID.
     */
    public static ApuId from(String uuidString) {
        return new ApuId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApuId that = (ApuId) o;
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
