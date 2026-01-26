package com.budgetpro.domain.finanzas.evm.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un Snapshot de EVM.
 */
public final class EVMSnapshotId {

    private final UUID value;

    private EVMSnapshotId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del snapshot de EVM no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo EVMSnapshotId.
     */
    public static EVMSnapshotId nuevo() {
        return new EVMSnapshotId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un EVMSnapshotId desde un UUID existente.
     */
    public static EVMSnapshotId from(UUID uuid) {
        return new EVMSnapshotId(uuid);
    }

    /**
     * Factory method para crear un EVMSnapshotId desde un String UUID.
     */
    public static EVMSnapshotId from(String uuidString) {
        return new EVMSnapshotId(UUID.fromString(uuidString));
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
        EVMSnapshotId that = (EVMSnapshotId) o;
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
