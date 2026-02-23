package com.budgetpro.domain.logistica.compra.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una Recepción.
 */
public final class RecepcionId {

    private final UUID value;

    private RecepcionId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la recepción no puede ser nulo");
    }

    /**
     * Factory method para crear un RecepcionId desde un UUID existente.
     */
    public static RecepcionId of(UUID uuid) {
        return new RecepcionId(uuid);
    }

    /**
     * Factory method para generar un nuevo RecepcionId.
     */
    public static RecepcionId generate() {
        return new RecepcionId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un nuevo RecepcionId.
     * @deprecated Use generate() instead
     */
    @Deprecated
    public static RecepcionId nuevo() {
        return generate();
    }

    /**
     * Factory method para crear un RecepcionId desde un UUID existente.
     * @deprecated Use of(UUID) instead
     */
    @Deprecated
    public static RecepcionId from(UUID uuid) {
        return of(uuid);
    }

    /**
     * Factory method para crear un RecepcionId desde un String UUID.
     */
    public static RecepcionId from(String uuidString) {
        return new RecepcionId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecepcionId that = (RecepcionId) o;
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
