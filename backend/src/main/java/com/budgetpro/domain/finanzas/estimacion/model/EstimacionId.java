package com.budgetpro.domain.finanzas.estimacion.model;

import java.util.Objects;
import java.util.UUID;

public final class EstimacionId {
    private final UUID value;

    private EstimacionId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la estimación no puede ser nulo");
    }

    public static EstimacionId of(UUID value) {
        return new EstimacionId(value);
    }

    public static EstimacionId random() {
        return new EstimacionId(UUID.randomUUID());
    }

    public static EstimacionId nuevo() {
        return random();
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
        EstimacionId that = (EstimacionId) o;
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
