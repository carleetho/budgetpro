package com.budgetpro.domain.logistica.inventario.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un movimiento de inventario.
 * Inmutable por diseño.
 */
public final class MovimientoInventarioId {

    private final UUID value;

    private MovimientoInventarioId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del MovimientoInventarioId no puede ser nulo");
        }
        this.value = value;
    }

    public static MovimientoInventarioId of(UUID value) {
        return new MovimientoInventarioId(value);
    }

    public static MovimientoInventarioId generate() {
        return new MovimientoInventarioId(UUID.randomUUID());
    }

    public static MovimientoInventarioId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del MovimientoInventarioId no puede ser nulo o vacío");
        }
        return new MovimientoInventarioId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimientoInventarioId that = (MovimientoInventarioId) o;
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
