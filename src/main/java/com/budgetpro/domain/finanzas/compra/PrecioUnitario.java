package com.budgetpro.domain.finanzas.compra;

import com.budgetpro.domain.finanzas.model.Monto;

import java.util.Objects;

/**
 * Value Object que encapsula un precio unitario.
 * 
 * Reutiliza el Value Object Monto para consistencia monetaria.
 * Escala: 4 decimales (NUMERIC(19,4) según ERD físico)
 * 
 * Invariante: El precio unitario debe ser mayor que cero.
 */
public final class PrecioUnitario {

    private final Monto value;

    /**
     * Constructor privado. Usar factory methods.
     */
    private PrecioUnitario(Monto value) {
        Objects.requireNonNull(value, "El valor del precio unitario no puede ser nulo");
        
        if (value.esMenorOIgualQue(Monto.cero())) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero");
        }
        
        this.value = value;
    }

    /**
     * Factory method para crear un PrecioUnitario desde Monto.
     */
    public static PrecioUnitario of(Monto value) {
        return new PrecioUnitario(value);
    }

    /**
     * Factory method para crear un PrecioUnitario desde BigDecimal.
     */
    public static PrecioUnitario of(java.math.BigDecimal value) {
        return new PrecioUnitario(Monto.of(value));
    }

    /**
     * Factory method para crear un PrecioUnitario desde double.
     */
    public static PrecioUnitario of(double value) {
        return new PrecioUnitario(Monto.of(value));
    }

    /**
     * Factory method para crear un PrecioUnitario desde String.
     */
    public static PrecioUnitario of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del precio unitario no puede ser nulo o vacío");
        }
        return new PrecioUnitario(Monto.of(value));
    }

    /**
     * Obtiene el valor como Monto.
     */
    public Monto getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrecioUnitario that = (PrecioUnitario) o;
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
