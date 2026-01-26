package com.budgetpro.domain.catalogo.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un APUSnapshot.
 * Inmutable por diseño.
 */
public final class APUSnapshotId {

    private final UUID value;

    private APUSnapshotId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del APUSnapshotId no puede ser nulo");
        }
        this.value = value;
    }

    public static APUSnapshotId of(UUID value) {
        return new APUSnapshotId(value);
    }

    public static APUSnapshotId generate() {
        return new APUSnapshotId(UUID.randomUUID());
    }

    public static APUSnapshotId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del APUSnapshotId no puede ser nulo o vacío");
        }
        return new APUSnapshotId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APUSnapshotId that = (APUSnapshotId) o;
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
