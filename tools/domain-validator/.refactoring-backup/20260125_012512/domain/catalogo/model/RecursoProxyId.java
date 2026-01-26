package com.budgetpro.domain.catalogo.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un RecursoProxy.
 * Inmutable por diseño.
 */
public final class RecursoProxyId {

    private final UUID value;

    private RecursoProxyId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del RecursoProxyId no puede ser nulo");
        }
        this.value = value;
    }

    public static RecursoProxyId of(UUID value) {
        return new RecursoProxyId(value);
    }

    public static RecursoProxyId generate() {
        return new RecursoProxyId(UUID.randomUUID());
    }

    public static RecursoProxyId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del RecursoProxyId no puede ser nulo o vacío");
        }
        return new RecursoProxyId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecursoProxyId that = (RecursoProxyId) o;
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
