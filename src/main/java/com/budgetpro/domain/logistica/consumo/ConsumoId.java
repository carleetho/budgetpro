package com.budgetpro.domain.logistica.consumo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula la identidad única de un Consumo.
 * Inmutable por diseño.
 */
public final class ConsumoId {

    private final UUID value;

    private ConsumoId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del ConsumoId no puede ser nulo");
        }
        this.value = value;
    }

    public static ConsumoId of(UUID value) {
        return new ConsumoId(value);
    }

    public static ConsumoId generate() {
        return new ConsumoId(UUID.randomUUID());
    }

    public static ConsumoId fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del ConsumoId no puede ser nulo o vacío");
        }
        return new ConsumoId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumoId consumoId = (ConsumoId) o;
        return Objects.equals(value, consumoId.value);
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
