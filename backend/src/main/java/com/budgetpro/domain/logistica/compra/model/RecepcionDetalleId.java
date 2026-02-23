package com.budgetpro.domain.logistica.compra.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un RecepcionDetalle.
 */
public final class RecepcionDetalleId {

    private final UUID value;

    private RecepcionDetalleId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del detalle de recepción no puede ser nulo");
    }

    /**
     * Factory method para crear un RecepcionDetalleId desde un UUID existente.
     */
    public static RecepcionDetalleId of(UUID uuid) {
        return new RecepcionDetalleId(uuid);
    }

    /**
     * Factory method para generar un nuevo RecepcionDetalleId.
     */
    public static RecepcionDetalleId generate() {
        return new RecepcionDetalleId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un nuevo RecepcionDetalleId.
     * @deprecated Use generate() instead
     */
    @Deprecated
    public static RecepcionDetalleId nuevo() {
        return generate();
    }

    /**
     * Factory method para crear un RecepcionDetalleId desde un UUID existente.
     * @deprecated Use of(UUID) instead
     */
    @Deprecated
    public static RecepcionDetalleId from(UUID uuid) {
        return of(uuid);
    }

    /**
     * Factory method para crear un RecepcionDetalleId desde un String UUID.
     */
    public static RecepcionDetalleId from(String uuidString) {
        return new RecepcionDetalleId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RecepcionDetalleId that = (RecepcionDetalleId) o;
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
