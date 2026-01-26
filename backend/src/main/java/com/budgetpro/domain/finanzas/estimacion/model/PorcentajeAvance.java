package com.budgetpro.domain.finanzas.estimacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa un porcentaje de avance. Invariante: Debe estar
 * entre 0 y 100. Precisión: 2 decimales.
 */
public final class PorcentajeAvance {

    private static final BigDecimal MIN = BigDecimal.ZERO;
    private static final BigDecimal MAX = new BigDecimal("100.00");

    private final BigDecimal value;

    private PorcentajeAvance(BigDecimal value) {
        this.value = validateAndRound(value);
    }

    public static PorcentajeAvance of(BigDecimal value) {
        return new PorcentajeAvance(value);
    }

    public static PorcentajeAvance zero() {
        return new PorcentajeAvance(BigDecimal.ZERO);
    }

    public static PorcentajeAvance cien() {
        return new PorcentajeAvance(new BigDecimal("100.00"));
    }

    private BigDecimal validateAndRound(BigDecimal val) {
        Objects.requireNonNull(val, "El porcentaje de avance no puede ser nulo");

        // Round to 2 decimals
        BigDecimal rounded = val.setScale(2, RoundingMode.HALF_UP);

        if (rounded.compareTo(MIN) < 0 || rounded.compareTo(MAX) > 0) {
            throw new IllegalArgumentException("El porcentaje de avance debe estar entre 0 y 100");
        }

        return rounded;
    }

    public BigDecimal getValue() {
        return value;
    }

    public PorcentajeAvance sumar(PorcentajeAvance other) {
        BigDecimal result = this.value.add(other.value);
        // Permitimos temporalmente exceder 100 en sumas intermedias?
        // El requerimiento dice "Cumulative progress validation" en EstimacionItem,
        // así que aquí solo validamos rango 0-100 para la instancia final.
        // Si la suma excede 100, lanzará excepción al crear la nueva instancia.
        return new PorcentajeAvance(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PorcentajeAvance that = (PorcentajeAvance) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString() + "%";
    }
}
