package com.budgetpro.domain.finanzas.sobrecosto.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una ConfiguracionLaboral.
 */
public final class ConfiguracionLaboralId {

    private final UUID value;

    private ConfiguracionLaboralId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la configuración laboral no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo ConfiguracionLaboralId.
     */
    public static ConfiguracionLaboralId nuevo() {
        return new ConfiguracionLaboralId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un ConfiguracionLaboralId desde un UUID existente.
     */
    public static ConfiguracionLaboralId of(UUID uuid) {
        return new ConfiguracionLaboralId(uuid);
    }

    /**
     * Factory method para crear un ConfiguracionLaboralId desde un String UUID.
     */
    public static ConfiguracionLaboralId from(String uuidString) {
        return new ConfiguracionLaboralId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfiguracionLaboralId that = (ConfiguracionLaboralId) o;
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
