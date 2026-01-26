package com.budgetpro.domain.finanzas.estimacion.model;

import java.util.Objects;
import java.util.UUID;

public final class AvancePartidaId {
    private final UUID value;

    private AvancePartidaId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de avance de partida no puede ser nulo");
    }

    public static AvancePartidaId of(UUID value) {
        return new AvancePartidaId(value);
    }

    public static AvancePartidaId random() {
        return new AvancePartidaId(UUID.randomUUID());
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
        AvancePartidaId that = (AvancePartidaId) o;
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
