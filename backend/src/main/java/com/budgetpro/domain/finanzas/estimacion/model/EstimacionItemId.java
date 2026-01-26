package com.budgetpro.domain.finanzas.estimacion.model;

import java.util.Objects;
import java.util.UUID;

public final class EstimacionItemId {
    private final UUID value;

    private EstimacionItemId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del item de estimación no puede ser nulo");
    }

    public static EstimacionItemId of(UUID value) {
        return new EstimacionItemId(value);
    }

    public static EstimacionItemId random() {
        return new EstimacionItemId(UUID.randomUUID());
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
        EstimacionItemId that = (EstimacionItemId) o;
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
