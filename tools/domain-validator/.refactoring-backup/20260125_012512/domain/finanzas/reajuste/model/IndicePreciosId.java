package com.budgetpro.domain.finanzas.reajuste.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el ID de un índice de precios.
 */
public final class IndicePreciosId {
    
    private final UUID value;
    
    private IndicePreciosId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del índice no puede ser nulo");
    }
    
    public static IndicePreciosId of(UUID value) {
        return new IndicePreciosId(value);
    }
    
    public static IndicePreciosId generate() {
        return new IndicePreciosId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndicePreciosId that = (IndicePreciosId) o;
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
