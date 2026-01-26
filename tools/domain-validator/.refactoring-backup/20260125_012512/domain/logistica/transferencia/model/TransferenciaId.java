package com.budgetpro.domain.logistica.transferencia.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de una transferencia entre
 * bodegas.
 * Inmutable por diseño.
 */
public final class TransferenciaId {

    private final UUID value;

    private TransferenciaId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del TransferenciaId no puede ser nulo");
        }
        this.value = value;
    }

    public static TransferenciaId of(UUID value) {
        return new TransferenciaId(value);
    }

    public static TransferenciaId generate() {
        return new TransferenciaId(UUID.randomUUID());
    }

    public static TransferenciaId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del TransferenciaId no puede ser nulo o vacío");
        }
        return new TransferenciaId(UUID.fromString(value));
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
        TransferenciaId that = (TransferenciaId) o;
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
