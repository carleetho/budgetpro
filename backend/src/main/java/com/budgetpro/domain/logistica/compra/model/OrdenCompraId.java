package com.budgetpro.domain.logistica.compra.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una OrdenCompra.
 */
public final class OrdenCompraId {

    private final UUID value;

    private OrdenCompraId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la orden de compra no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo OrdenCompraId.
     */
    public static OrdenCompraId nuevo() {
        return new OrdenCompraId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un OrdenCompraId desde un UUID existente.
     */
    public static OrdenCompraId from(UUID uuid) {
        return new OrdenCompraId(uuid);
    }

    /**
     * Factory method para crear un OrdenCompraId desde un String UUID.
     */
    public static OrdenCompraId from(String uuidString) {
        return new OrdenCompraId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenCompraId that = (OrdenCompraId) o;
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
