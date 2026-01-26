package com.budgetpro.domain.finanzas.estimacion.model;

import java.util.Objects;
import java.util.UUID;

public final class EstimacionSnapshotId {
    private final UUID value;

    private EstimacionSnapshotId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del snapshot de estimación no puede ser nulo");
    }

    public static EstimacionSnapshotId of(UUID value) {
        return new EstimacionSnapshotId(value);
    }

    public static EstimacionSnapshotId random() {
        return new EstimacionSnapshotId(UUID.randomUUID());
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
        EstimacionSnapshotId that = (EstimacionSnapshotId) o;
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
