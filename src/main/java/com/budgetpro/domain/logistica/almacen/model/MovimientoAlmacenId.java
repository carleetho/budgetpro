package com.budgetpro.domain.logistica.almacen.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el ID de un movimiento de almacén.
 */
public final class MovimientoAlmacenId {
    
    private final UUID value;
    
    private MovimientoAlmacenId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del movimiento no puede ser nulo");
    }
    
    public static MovimientoAlmacenId of(UUID value) {
        return new MovimientoAlmacenId(value);
    }
    
    public static MovimientoAlmacenId generate() {
        return new MovimientoAlmacenId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimientoAlmacenId that = (MovimientoAlmacenId) o;
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
