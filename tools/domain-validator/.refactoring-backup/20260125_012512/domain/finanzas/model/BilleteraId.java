package com.budgetpro.domain.finanzas.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de una Billetera.
 * Inmutable por diseño.
 */
public final class BilleteraId {

    private final UUID value;

    private BilleteraId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del BilleteraId no puede ser nulo");
        }
        this.value = value;
    }

    public static BilleteraId of(UUID value) {
        return new BilleteraId(value);
    }

    public static BilleteraId generate() {
        return new BilleteraId(UUID.randomUUID());
    }

    public static BilleteraId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del BilleteraId no puede ser nulo o vacío");
        }
        return new BilleteraId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BilleteraId that = (BilleteraId) o;
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
