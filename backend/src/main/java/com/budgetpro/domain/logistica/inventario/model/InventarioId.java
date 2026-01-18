package com.budgetpro.domain.logistica.inventario.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un item de inventario.
 * Inmutable por diseño.
 */
public final class InventarioId {

    private final UUID value;

    private InventarioId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del InventarioId no puede ser nulo");
        }
        this.value = value;
    }

    public static InventarioId of(UUID value) {
        return new InventarioId(value);
    }

    public static InventarioId generate() {
        return new InventarioId(UUID.randomUUID());
    }

    public static InventarioId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del InventarioId no puede ser nulo o vacío");
        }
        return new InventarioId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarioId that = (InventarioId) o;
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
