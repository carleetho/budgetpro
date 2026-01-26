package com.budgetpro.domain.logistica.compra.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una Compra.
 */
public final class CompraId {

    private final UUID value;

    private CompraId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la compra no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo CompraId.
     */
    public static CompraId nuevo() {
        return new CompraId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un CompraId desde un UUID existente.
     */
    public static CompraId from(UUID uuid) {
        return new CompraId(uuid);
    }

    /**
     * Factory method para crear un CompraId desde un String UUID.
     */
    public static CompraId from(String uuidString) {
        return new CompraId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompraId that = (CompraId) o;
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
