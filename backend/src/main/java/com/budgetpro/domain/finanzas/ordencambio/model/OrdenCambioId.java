package com.budgetpro.domain.finanzas.ordencambio.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una Orden de Cambio.
 */
public final class OrdenCambioId {

    private final UUID value;

    private OrdenCambioId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la orden de cambio no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo OrdenCambioId.
     */
    public static OrdenCambioId nuevo() {
        return new OrdenCambioId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un OrdenCambioId desde un UUID existente.
     */
    public static OrdenCambioId from(UUID uuid) {
        return new OrdenCambioId(uuid);
    }

    /**
     * Factory method para crear un OrdenCambioId desde un String UUID.
     */
    public static OrdenCambioId from(String uuidString) {
        return new OrdenCambioId(UUID.fromString(uuidString));
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
        OrdenCambioId that = (OrdenCambioId) o;
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
