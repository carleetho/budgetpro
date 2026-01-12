package com.budgetpro.domain.finanzas.presupuesto;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un Presupuesto.
 * Inmutable por diseño.
 */
public final class PresupuestoId {

    private final UUID value;

    private PresupuestoId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del PresupuestoId no puede ser nulo");
        }
        this.value = value;
    }

    public static PresupuestoId of(UUID value) {
        return new PresupuestoId(value);
    }

    public static PresupuestoId generate() {
        return new PresupuestoId(UUID.randomUUID());
    }

    public static PresupuestoId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del PresupuestoId no puede ser nulo o vacío");
        }
        return new PresupuestoId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
