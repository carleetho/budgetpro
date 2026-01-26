package com.budgetpro.domain.catalogo.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un APUInsumoSnapshot.
 * Inmutable por diseño.
 */
public final class APUInsumoSnapshotId {

    private final UUID value;

    private APUInsumoSnapshotId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del APUInsumoSnapshotId no puede ser nulo");
        }
        this.value = value;
    }

    public static APUInsumoSnapshotId of(UUID value) {
        return new APUInsumoSnapshotId(value);
    }

    public static APUInsumoSnapshotId generate() {
        return new APUInsumoSnapshotId(UUID.randomUUID());
    }

    public static APUInsumoSnapshotId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del APUInsumoSnapshotId no puede ser nulo o vacío");
        }
        return new APUInsumoSnapshotId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APUInsumoSnapshotId that = (APUInsumoSnapshotId) o;
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
