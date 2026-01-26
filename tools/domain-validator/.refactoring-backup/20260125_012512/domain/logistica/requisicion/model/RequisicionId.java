package com.budgetpro.domain.logistica.requisicion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de una Requisición.
 * Inmutable por diseño.
 */
public final class RequisicionId {

    private final UUID value;

    private RequisicionId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del RequisicionId no puede ser nulo");
        }
        this.value = value;
    }

    public static RequisicionId of(UUID value) {
        return new RequisicionId(value);
    }

    public static RequisicionId generate() {
        return new RequisicionId(UUID.randomUUID());
    }

    public static RequisicionId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del RequisicionId no puede ser nulo o vacío");
        }
        return new RequisicionId(UUID.fromString(value));
    }

    public static RequisicionId from(UUID value) {
        return of(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequisicionId that = (RequisicionId) o;
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
