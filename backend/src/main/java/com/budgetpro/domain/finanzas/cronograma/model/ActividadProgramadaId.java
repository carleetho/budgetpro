package com.budgetpro.domain.finanzas.cronograma.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una ActividadProgramada.
 */
public final class ActividadProgramadaId {

    private final UUID value;

    private ActividadProgramadaId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la actividad programada no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ActividadProgramadaId.
     */
    public static ActividadProgramadaId nuevo() {
        return new ActividadProgramadaId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ActividadProgramadaId desde un UUID existente.
     */
    public static ActividadProgramadaId of(UUID uuid) {
        return new ActividadProgramadaId(uuid);
    }

    /**
     * Factory method para crear un ActividadProgramadaId desde un String UUID.
     */
    public static ActividadProgramadaId from(String uuidString) {
        return new ActividadProgramadaId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActividadProgramadaId that = (ActividadProgramadaId) o;
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
