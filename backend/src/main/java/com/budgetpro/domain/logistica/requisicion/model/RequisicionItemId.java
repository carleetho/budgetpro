package com.budgetpro.domain.logistica.requisicion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un RequisicionItem.
 * Inmutable por diseño.
 */
public final class RequisicionItemId {

    private final UUID value;

    private RequisicionItemId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del RequisicionItemId no puede ser nulo");
        }
        this.value = value;
    }

    public static RequisicionItemId of(UUID value) {
        return new RequisicionItemId(value);
    }

    public static RequisicionItemId generate() {
        return new RequisicionItemId(UUID.randomUUID());
    }

    public static RequisicionItemId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del RequisicionItemId no puede ser nulo o vacío");
        }
        return new RequisicionItemId(UUID.fromString(value));
    }

    public static RequisicionItemId from(UUID value) {
        return of(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequisicionItemId that = (RequisicionItemId) o;
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
