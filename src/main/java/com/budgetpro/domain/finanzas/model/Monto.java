package com.budgetpro.domain.finanzas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que encapsula un monto monetario con precisión fija.
 * 
 * Escala: 4 decimales (NUMERIC(19,4) según ERD físico)
 * Redondeo: HALF_EVEN (Banker's Rounding) para evitar sesgos estadísticos
 * 
 * Este Value Object es compartido entre los agregados Billetera y Partida.
 */
public final class Monto {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private final BigDecimal value;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Monto(BigDecimal value) {
        Objects.requireNonNull(value, "El valor del monto no puede ser nulo");
        
        // Normalizar a escala 4 con redondeo HALF_EVEN
        this.value = value.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Factory method para crear un Monto desde BigDecimal.
     */
    public static Monto of(BigDecimal value) {
        return new Monto(value);
    }

    /**
     * Factory method para crear un Monto desde double.
     */
    public static Monto of(double value) {
        return new Monto(BigDecimal.valueOf(value));
    }

    /**
     * Factory method para crear un Monto desde String.
     */
    public static Monto of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del monto no puede ser nulo o vacío");
        }
        return new Monto(new BigDecimal(value));
    }

    /**
     * Factory method para crear un Monto con valor cero.
     */
    public static Monto cero() {
        return new Monto(BigDecimal.ZERO);
    }

    /**
     * Suma este monto con otro.
     */
    public Monto sumar(Monto otro) {
        Objects.requireNonNull(otro, "El monto a sumar no puede ser nulo");
        return new Monto(this.value.add(otro.value));
    }

    /**
     * Resta otro monto de este.
     */
    public Monto restar(Monto otro) {
        Objects.requireNonNull(otro, "El monto a restar no puede ser nulo");
        return new Monto(this.value.subtract(otro.value));
    }

    /**
     * Multiplica este monto por un factor.
     */
    public Monto multiplicar(BigDecimal factor) {
        Objects.requireNonNull(factor, "El factor no puede ser nulo");
        return new Monto(this.value.multiply(factor));
    }

    /**
     * Verifica si este monto es mayor que otro.
     */
    public boolean esMayorQue(Monto otro) {
        Objects.requireNonNull(otro, "El monto a comparar no puede ser nulo");
        return this.value.compareTo(otro.value) > 0;
    }

    /**
     * Verifica si este monto es menor que otro.
     */
    public boolean esMenorQue(Monto otro) {
        Objects.requireNonNull(otro, "El monto a comparar no puede ser nulo");
        return this.value.compareTo(otro.value) < 0;
    }

    /**
     * Verifica si este monto es mayor o igual que otro.
     */
    public boolean esMayorOIgualQue(Monto otro) {
        Objects.requireNonNull(otro, "El monto a comparar no puede ser nulo");
        return this.value.compareTo(otro.value) >= 0;
    }

    /**
     * Verifica si este monto es menor o igual que otro.
     */
    public boolean esMenorOIgualQue(Monto otro) {
        Objects.requireNonNull(otro, "El monto a comparar no puede ser nulo");
        return this.value.compareTo(otro.value) <= 0;
    }

    /**
     * Verifica si este monto es negativo.
     */
    public boolean esNegativo() {
        return this.value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Verifica si este monto es cero.
     */
    public boolean esCero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Retorna el valor absoluto de este monto.
     */
    public Monto absoluto() {
        return new Monto(this.value.abs());
    }

    /**
     * Obtiene el valor como BigDecimal con escala 4.
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Convierte este monto a BigDecimal con escala 4.
     * Alias de getValue() para claridad semántica.
     */
    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Monto monto = (Monto) o;
        return Objects.equals(value, monto.value);
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
