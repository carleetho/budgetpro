package com.budgetpro.domain.finanzas.compra;

import com.budgetpro.domain.finanzas.model.Monto;

import java.util.Objects;

/**
 * Value Object que encapsula el total de una compra.
 * 
 * Reutiliza el Value Object Monto para consistencia monetaria.
 * Escala: 4 decimales (NUMERIC(19,4) según ERD físico)
 * 
 * Este valor es derivado (calculado) y no debe ser seteable desde fuera del agregado.
 */
public final class TotalCompra {

    private final Monto value;

    /**
     * Constructor privado. Usar factory methods.
     */
    private TotalCompra(Monto value) {
        Objects.requireNonNull(value, "El valor del total no puede ser nulo");
        this.value = value;
    }

    /**
     * Factory method para crear un TotalCompra desde Monto.
     */
    public static TotalCompra of(Monto value) {
        return new TotalCompra(value);
    }

    /**
     * Factory method para crear un TotalCompra desde BigDecimal.
     */
    public static TotalCompra of(java.math.BigDecimal value) {
        return new TotalCompra(Monto.of(value));
    }

    /**
     * Factory method para crear un TotalCompra desde double.
     */
    public static TotalCompra of(double value) {
        return new TotalCompra(Monto.of(value));
    }

    /**
     * Factory method para crear un TotalCompra con valor cero.
     */
    public static TotalCompra cero() {
        return new TotalCompra(Monto.cero());
    }

    /**
     * Suma este total con otro.
     */
    public TotalCompra sumar(TotalCompra otro) {
        Objects.requireNonNull(otro, "El total a sumar no puede ser nulo");
        return new TotalCompra(this.value.sumar(otro.value));
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
        TotalCompra that = (TotalCompra) o;
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
