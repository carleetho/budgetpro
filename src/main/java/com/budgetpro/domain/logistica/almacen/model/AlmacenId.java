package com.budgetpro.domain.logistica.almacen.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el ID de un almacén.
 */
public final class AlmacenId {
    
    private final UUID value;
    
    private AlmacenId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del almacén no puede ser nulo");
    }
    
    public static AlmacenId of(UUID value) {
        return new AlmacenId(value);
    }
    
    public static AlmacenId generate() {
        return new AlmacenId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlmacenId that = (AlmacenId) o;
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
