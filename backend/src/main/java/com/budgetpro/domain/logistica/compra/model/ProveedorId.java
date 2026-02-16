package com.budgetpro.domain.logistica.compra.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un Proveedor.
 */
public final class ProveedorId {

    private final UUID value;

    private ProveedorId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del proveedor no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ProveedorId.
     */
    public static ProveedorId nuevo() {
        return new ProveedorId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ProveedorId desde un UUID existente.
     */
    public static ProveedorId from(UUID uuid) {
        return new ProveedorId(uuid);
    }

    /**
     * Factory method para crear un ProveedorId desde un String UUID.
     */
    public static ProveedorId from(String uuidString) {
        return new ProveedorId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProveedorId that = (ProveedorId) o;
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
