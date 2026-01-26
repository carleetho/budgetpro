package com.budgetpro.domain.rrhh.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object encapsulating the unique identity of a Project Assignment.
 * Immutable by design.
 */
public final class AsignacionProyectoId {

    private final UUID value;

    private AsignacionProyectoId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("AsignacionProyectoId value cannot be null");
        }
        this.value = value;
    }

    public static AsignacionProyectoId of(UUID value) {
        return new AsignacionProyectoId(value);
    }

    public static AsignacionProyectoId generate() {
        return new AsignacionProyectoId(UUID.randomUUID());
    }

    public static AsignacionProyectoId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AsignacionProyectoId value cannot be null or empty");
        }
        return new AsignacionProyectoId(UUID.fromString(value));
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
        AsignacionProyectoId that = (AsignacionProyectoId) o;
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
