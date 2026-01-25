package com.budgetpro.domain.rrhh.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating the unique identity of a Crew (Cuadrilla).
 */
public final class CuadrillaId {

    private final UUID value;

    private CuadrillaId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CuadrillaId value cannot be null");
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
            throw new IllegalArgumentException("CuadrillaId value cannot be null or empty");
        }
        return new CuadrillaId(UUID.fromString(value));
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
