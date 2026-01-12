package com.budgetpro.domain.finanzas.compra;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que encapsula una cantidad física (unidades, metros, etc.).
 * 
 * Escala: 4 decimales para precisión en mediciones
 * Redondeo: HALF_EVEN (Banker's Rounding)
 * 
 * Invariante: La cantidad debe ser mayor que cero.
 */
public final class Cantidad {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private final BigDecimal value;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Cantidad(BigDecimal value) {
        Objects.requireNonNull(value, "El valor de la cantidad no puede ser nulo");
        
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        
        // Normalizar a escala 4 con redondeo HALF_EVEN
        this.value = value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Factory method para crear una Cantidad desde BigDecimal.
     */
    public static Cantidad of(BigDecimal value) {
        return new Cantidad(value);
    }

    /**
     * Factory method para crear una Cantidad desde double.
     */
    public static Cantidad of(double value) {
        return new Cantidad(BigDecimal.valueOf(value));
    }

    /**
     * Factory method para crear una Cantidad desde String.
     */
    public static Cantidad of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor de la cantidad no puede ser nulo o vacío");
        }
        return new Cantidad(new BigDecimal(value));
    }

    /**
     * Multiplica esta cantidad por un precio unitario para obtener un total.
     */
    public TotalCompra multiplicar(PrecioUnitario precioUnitario) {
        Objects.requireNonNull(precioUnitario, "El precio unitario no puede ser nulo");
        java.math.BigDecimal totalValue = this.value.multiply(precioUnitario.getValue().getValue());
        return TotalCompra.of(totalValue);
    }

    /**
     * Obtiene el valor como BigDecimal con escala 4.
     */
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cantidad cantidad = (Cantidad) o;
        return Objects.equals(value, cantidad.value);
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
