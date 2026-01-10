package com.budgetpro.domain.finanzas.billetera;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que encapsula un monto monetario con precisión de 4 decimales.
 * 
 * Alineado con el ERD Físico Definitivo: NUMERIC(19,4)
 * 
 * Invariantes:
 * - Escala fija de 4 decimales (NUMERIC(19,4))
 * - Redondeo HALF_EVEN (Banker's Rounding)
 * - No puede ser negativo (validado en operaciones)
 * - Inmutable por diseño
 */
public final class Monto {

    private static final int ESCALA = 4;
    private static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_EVEN;
    
    private final BigDecimal value;

    private Monto(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("El valor del monto no puede ser nulo");
        }
        this.value = value.setScale(ESCALA, MODO_REDONDEO);
    }

    /**
     * Crea un Monto desde un BigDecimal.
     * Aplica redondeo a 4 decimales con HALF_EVEN (alineado con NUMERIC(19,4)).
     */
    public static Monto of(BigDecimal value) {
        return new Monto(value);
    }

    /**
     * Crea un Monto desde un double.
     * Aplica redondeo a 4 decimales con HALF_EVEN (alineado con NUMERIC(19,4)).
     */
    public static Monto of(double value) {
        return new Monto(BigDecimal.valueOf(value));
    }

    /**
     * Crea un Monto desde un String.
     * Aplica redondeo a 4 decimales con HALF_EVEN (alineado con NUMERIC(19,4)).
     */
    public static Monto of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El valor del monto no puede ser nulo o vacío");
        }
        return new Monto(new BigDecimal(value));
    }

    /**
     * Crea un Monto de valor ZERO.
     */
    public static Monto cero() {
        return new Monto(BigDecimal.ZERO);
    }

    /**
     * Suma este monto con otro.
     * 
     * @param otro El monto a sumar
     * @return Un nuevo Monto con el resultado de la suma
     */
    public Monto sumar(Monto otro) {
        Objects.requireNonNull(otro, "El monto a sumar no puede ser nulo");
        return new Monto(this.value.add(otro.value));
    }

    /**
     * Resta otro monto de este.
     * 
     * @param otro El monto a restar
     * @return Un nuevo Monto con el resultado de la resta
     */
    public Monto restar(Monto otro) {
        Objects.requireNonNull(otro, "El monto a restar no puede ser nulo");
        return new Monto(this.value.subtract(otro.value));
    }

    /**
     * Multiplica este monto por un factor.
     * 
     * @param factor El factor de multiplicación
     * @return Un nuevo Monto con el resultado de la multiplicación
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
     * Verifica si este monto es mayor o igual que otro.
     */
    public boolean esMayorOIgualQue(Monto otro) {
        Objects.requireNonNull(otro, "El monto a comparar no puede ser nulo");
        return this.value.compareTo(otro.value) >= 0;
    }

    /**
     * Verifica si este monto es menor que otro.
     */
    public boolean esMenorQue(Monto otro) {
        Objects.requireNonNull(otro, "El monto a comparar no puede ser nulo");
        return this.value.compareTo(otro.value) < 0;
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
        if (this.value.compareTo(BigDecimal.ZERO) >= 0) {
            return this;
        }
        return new Monto(this.value.negate());
    }

    /**
     * Retorna el BigDecimal subyacente.
     * Este método debe usarse con precaución, solo cuando sea necesario
     * para interacción con APIs externas o persistencia.
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Retorna el BigDecimal con la escala configurada (4 decimales).
     * Alineado con NUMERIC(19,4) del ERD Físico Definitivo.
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
