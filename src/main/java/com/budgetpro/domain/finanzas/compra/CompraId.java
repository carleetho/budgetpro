package com.budgetpro.domain.finanzas.compra;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de una Compra.
 * Inmutable por diseño.
 */
public final class CompraId {

    private final UUID value;

    private CompraId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del CompraId no puede ser nulo");
        }
        this.value = value;
    }

    public static CompraId of(UUID value) {
        return new CompraId(value);
    }

    public static CompraId generate() {
        return new CompraId(UUID.randomUUID());
    }

    public static CompraId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del CompraId no puede ser nulo o vacío");
        }
        return new CompraId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompraId compraId = (CompraId) o;
        return Objects.equals(value, compraId.value);
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
