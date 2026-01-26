package com.budgetpro.domain.rrhh.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating the unique identity of an Employee. Immutable by
 * design.
 */
public final class EmpleadoId {

    private final UUID value;

    private EmpleadoId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("EmpleadoId value cannot be null");
        }
        this.value = value;
    }

    public static EmpleadoId of(UUID value) {
        return new EmpleadoId(value);
    }

    public static EmpleadoId generate() {
        return new EmpleadoId(UUID.randomUUID());
    }

    public static EmpleadoId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EmpleadoId value cannot be null or empty");
        }
        return new EmpleadoId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EmpleadoId that = (EmpleadoId) o;
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
