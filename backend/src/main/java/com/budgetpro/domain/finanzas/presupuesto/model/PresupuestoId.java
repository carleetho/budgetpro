package com.budgetpro.domain.finanzas.presupuesto.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un Presupuesto.
 */
public final class PresupuestoId {

    private final UUID value;

    private PresupuestoId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del presupuesto no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo PresupuestoId.
     */
    public static PresupuestoId nuevo() {
        return new PresupuestoId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un PresupuestoId desde un UUID existente.
     */
    public static PresupuestoId from(UUID uuid) {
        return new PresupuestoId(uuid);
    }

    /**
     * Factory method para crear un PresupuestoId desde un UUID existente (Alias
     * common).
     */
    public static PresupuestoId of(UUID uuid) {
        return new PresupuestoId(uuid);
    }

    /**
     * Factory method para crear un PresupuestoId desde un String UUID.
     */
    public static PresupuestoId from(String uuidString) {
        return new PresupuestoId(UUID.fromString(uuidString));
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
        PresupuestoId that = (PresupuestoId) o;
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
