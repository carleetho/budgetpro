package com.budgetpro.domain.rrhh.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class AsistenciaId implements Serializable {
    private final UUID value;

    public AsistenciaId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("AsistenciaId cannot be null");
        }
        this.value = value;
    }

    public static AsistenciaId of(UUID value) {
        return new AsistenciaId(value);
    }

    public static AsistenciaId random() {
        return new AsistenciaId(UUID.randomUUID());
    }

    public static AsistenciaId fromString(String value) {
        return new AsistenciaId(UUID.fromString(value));
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
        AsistenciaId that = (AsistenciaId) o;
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
