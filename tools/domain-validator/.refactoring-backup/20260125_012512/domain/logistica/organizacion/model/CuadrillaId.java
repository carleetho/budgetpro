package com.budgetpro.domain.logistica.organizacion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de una Cuadrilla.
 * Inmutable por diseño.
 */
public final class CuadrillaId {

    private final UUID value;

    private CuadrillaId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del CuadrillaId no puede ser nulo");
        }
        this.value = value;
    }

    public static CuadrillaId of(UUID value) {
        return new CuadrillaId(value);
    }

    public static CuadrillaId generate() {
        return new CuadrillaId(UUID.randomUUID());
    }

    public static CuadrillaId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del CuadrillaId no puede ser nulo o vacío");
        }
        return new CuadrillaId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CuadrillaId that = (CuadrillaId) o;
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
