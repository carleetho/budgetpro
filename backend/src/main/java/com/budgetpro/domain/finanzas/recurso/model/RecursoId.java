package com.budgetpro.domain.finanzas.recurso.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un Recurso. Inmutable por
 * diseño.
 */
public final class RecursoId {

    private final UUID value;

    private RecursoId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del RecursoId no puede ser nulo");
        }
        this.value = value;
    }

    public static RecursoId of(UUID value) {
        return new RecursoId(value);
    }

    public static RecursoId generate() {
        return new RecursoId(UUID.randomUUID());
    }

    public static RecursoId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del RecursoId no puede ser nulo o vacío");
        }
        return new RecursoId(UUID.fromString(value));
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
        RecursoId recursoId = (RecursoId) o;
        return Objects.equals(value, recursoId.value);
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
