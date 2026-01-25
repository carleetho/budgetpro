package com.budgetpro.domain.rrhh.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating the unique identity of a Crew Memeber (Cuadrilla
 * Miembro).
 */
public final class CuadrillaMiembroId {

    private final UUID value;

    private CuadrillaMiembroId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CuadrillaMiembroId value cannot be null");
        }
        this.value = value;
    }

    public static CuadrillaMiembroId of(UUID value) {
        return new CuadrillaMiembroId(value);
    }

    public static CuadrillaMiembroId generate() {
        return new CuadrillaMiembroId(UUID.randomUUID());
    }

    public static CuadrillaMiembroId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CuadrillaMiembroId value cannot be null or empty");
        }
        return new CuadrillaMiembroId(UUID.fromString(value));
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
        CuadrillaMiembroId that = (CuadrillaMiembroId) o;
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
