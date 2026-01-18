package com.budgetpro.domain.finanzas.cronograma.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de un ProgramaObra.
 */
public final class ProgramaObraId {

    private final UUID value;

    private ProgramaObraId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del programa de obra no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ProgramaObraId.
     */
    public static ProgramaObraId nuevo() {
        return new ProgramaObraId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ProgramaObraId desde un UUID existente.
     */
    public static ProgramaObraId of(UUID uuid) {
        return new ProgramaObraId(uuid);
    }

    /**
     * Factory method para crear un ProgramaObraId desde un String UUID.
     */
    public static ProgramaObraId from(String uuidString) {
        return new ProgramaObraId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramaObraId that = (ProgramaObraId) o;
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
