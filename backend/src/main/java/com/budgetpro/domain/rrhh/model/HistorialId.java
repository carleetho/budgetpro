package com.budgetpro.domain.rrhh.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating the unique identity of a HistorialLaboral record.
 * Immutable by design.
 */
public final class HistorialId {

    private final UUID value;

    private HistorialId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("HistorialId value cannot be null");
        }
        this.value = value;
    }

    public static HistorialId of(UUID value) {
        return new HistorialId(value);
    }

    public static HistorialId generate() {
        return new HistorialId(UUID.randomUUID());
    }

    public static HistorialId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("HistorialId value cannot be null or empty");
        }
        return new HistorialId(UUID.fromString(value));
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
        HistorialId that = (HistorialId) o;
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
