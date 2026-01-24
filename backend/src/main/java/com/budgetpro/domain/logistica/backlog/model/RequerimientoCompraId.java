package com.budgetpro.domain.logistica.backlog.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un RequerimientoCompra.
 * Inmutable por diseño.
 */
public final class RequerimientoCompraId {

    private final UUID value;

    private RequerimientoCompraId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del RequerimientoCompraId no puede ser nulo");
        }
        this.value = value;
    }

    public static RequerimientoCompraId of(UUID value) {
        return new RequerimientoCompraId(value);
    }

    public static RequerimientoCompraId generate() {
        return new RequerimientoCompraId(UUID.randomUUID());
    }

    public static RequerimientoCompraId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del RequerimientoCompraId no puede ser nulo o vacío");
        }
        return new RequerimientoCompraId(UUID.fromString(value));
    }

    public static RequerimientoCompraId from(UUID value) {
        return of(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequerimientoCompraId that = (RequerimientoCompraId) o;
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
