package com.budgetpro.domain.finanzas.cronograma.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un CronogramaSnapshot.
 */
public final class CronogramaSnapshotId {

    private final UUID value;

    private CronogramaSnapshotId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del snapshot del cronograma no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo CronogramaSnapshotId.
     */
    public static CronogramaSnapshotId nuevo() {
        return new CronogramaSnapshotId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un CronogramaSnapshotId desde un UUID existente.
     */
    public static CronogramaSnapshotId of(UUID uuid) {
        return new CronogramaSnapshotId(uuid);
    }

    /**
     * Factory method para crear un CronogramaSnapshotId desde un String UUID.
     */
    public static CronogramaSnapshotId from(String uuidString) {
        return new CronogramaSnapshotId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CronogramaSnapshotId that = (CronogramaSnapshotId) o;
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
