package com.budgetpro.domain.logistica.bodega.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de una Bodega (almacén físico).
 * Inmutable por diseño.
 */
public final class BodegaId {

    private final UUID value;

    private BodegaId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del BodegaId no puede ser nulo");
        }
        this.value = value;
    }

    public static BodegaId of(UUID value) {
        return new BodegaId(value);
    }

    public static BodegaId generate() {
        return new BodegaId(UUID.randomUUID());
    }

    public static BodegaId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del BodegaId no puede ser nulo o vacío");
        }
        return new BodegaId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BodegaId that = (BodegaId) o;
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
