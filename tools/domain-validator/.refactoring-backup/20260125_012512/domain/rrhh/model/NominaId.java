package com.budgetpro.domain.rrhh.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class NominaId implements Serializable {
    private final UUID value;

    private NominaId(UUID value) {
        this.value = Objects.requireNonNull(value, "NominaId value must not be null");
    }

    public static NominaId of(UUID value) {
        return new NominaId(value);
    }

    public static NominaId of(String value) {
        return new NominaId(UUID.fromString(value));
    }

    public static NominaId random() {
        return new NominaId(UUID.randomUUID());
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
        NominaId nominaId = (NominaId) o;
        return Objects.equals(value, nominaId.value);
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
