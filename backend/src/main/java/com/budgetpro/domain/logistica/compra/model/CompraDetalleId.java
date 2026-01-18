package com.budgetpro.domain.logistica.compra.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un CompraDetalle.
 */
public final class CompraDetalleId {

    private final UUID value;

    private CompraDetalleId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del detalle de compra no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo CompraDetalleId.
     */
    public static CompraDetalleId nuevo() {
        return new CompraDetalleId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un CompraDetalleId desde un UUID existente.
     */
    public static CompraDetalleId from(UUID uuid) {
        return new CompraDetalleId(uuid);
    }

    /**
     * Factory method para crear un CompraDetalleId desde un String UUID.
     */
    public static CompraDetalleId from(String uuidString) {
        return new CompraDetalleId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompraDetalleId that = (CompraDetalleId) o;
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
