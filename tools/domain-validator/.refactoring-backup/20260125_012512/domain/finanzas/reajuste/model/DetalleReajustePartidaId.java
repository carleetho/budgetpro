package com.budgetpro.domain.finanzas.reajuste.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el ID de un detalle de reajuste por partida.
 */
public final class DetalleReajustePartidaId {
    
    private final UUID value;
    
    private DetalleReajustePartidaId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID del detalle de reajuste no puede ser nulo");
    }
    
    public static DetalleReajustePartidaId of(UUID value) {
        return new DetalleReajustePartidaId(value);
    }
    
    public static DetalleReajustePartidaId generate() {
        return new DetalleReajustePartidaId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleReajustePartidaId that = (DetalleReajustePartidaId) o;
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
