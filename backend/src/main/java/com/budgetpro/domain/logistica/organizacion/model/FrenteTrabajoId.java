package com.budgetpro.domain.logistica.organizacion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un FrenteTrabajo.
 * Inmutable por diseño.
 */
public final class FrenteTrabajoId {

    private final UUID value;

    private FrenteTrabajoId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del FrenteTrabajoId no puede ser nulo");
        }
        this.value = value;
    }

    public static FrenteTrabajoId of(UUID value) {
        return new FrenteTrabajoId(value);
    }

    public static FrenteTrabajoId generate() {
        return new FrenteTrabajoId(UUID.randomUUID());
    }

    public static FrenteTrabajoId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del FrenteTrabajoId no puede ser nulo o vacío");
        }
        return new FrenteTrabajoId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrenteTrabajoId that = (FrenteTrabajoId) o;
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
