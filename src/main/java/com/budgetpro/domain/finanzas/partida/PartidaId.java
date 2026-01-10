package com.budgetpro.domain.finanzas.partida;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de una Partida.
 * Inmutable por diseño.
 */
public final class PartidaId {

    private final UUID value;

    private PartidaId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del PartidaId no puede ser nulo");
        }
        this.value = value;
    }

    public static PartidaId of(UUID value) {
        return new PartidaId(value);
    }

    public static PartidaId generate() {
        return new PartidaId(UUID.randomUUID());
    }

    public static PartidaId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del PartidaId no puede ser nulo o vacío");
        }
        return new PartidaId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartidaId partidaId = (PartidaId) o;
        return Objects.equals(value, partidaId.value);
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
