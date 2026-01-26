package com.budgetpro.domain.finanzas.reajuste.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el ID de una estimación de reajuste.
 */
public final class EstimacionReajusteId {
    
    private final UUID value;
    
    private EstimacionReajusteId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la estimación de reajuste no puede ser nulo");
    }
    
    public static EstimacionReajusteId of(UUID value) {
        return new EstimacionReajusteId(value);
    }
    
    public static EstimacionReajusteId generate() {
        return new EstimacionReajusteId(UUID.randomUUID());
    }
    
    public UUID getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstimacionReajusteId that = (EstimacionReajusteId) o;
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
