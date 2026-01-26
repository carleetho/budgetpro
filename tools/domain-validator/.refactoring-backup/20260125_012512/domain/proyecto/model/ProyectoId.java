package com.budgetpro.domain.proyecto.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un Proyecto.
 * 
 * Encapsula un UUID para garantizar type safety y evitar primitivos obsesivos.
 */
public final class ProyectoId {

    private final UUID value;

    private ProyectoId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del proyecto no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ProyectoId.
     */
    public static ProyectoId nuevo() {
        return new ProyectoId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ProyectoId desde un UUID existente.
     */
    public static ProyectoId from(UUID uuid) {
        return new ProyectoId(uuid);
    }

    /**
     * Factory method para crear un ProyectoId desde un String UUID.
     */
    public static ProyectoId from(String uuidString) {
        return new ProyectoId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProyectoId that = (ProyectoId) o;
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
