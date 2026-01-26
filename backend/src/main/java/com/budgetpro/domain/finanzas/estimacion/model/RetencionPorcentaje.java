package com.budgetpro.domain.finanzas.estimacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa el porcentaje de retención (fondo de garantía).
 * Invariante: 0-100. Default: 10%.
 */
public final class RetencionPorcentaje {

    private static final BigDecimal MIN = BigDecimal.ZERO;
    private static final BigDecimal MAX = new BigDecimal("100.00");
    private static final BigDecimal DEFAULT_VALUE = new BigDecimal("10.00");

    private final BigDecimal value;

    private RetencionPorcentaje(BigDecimal value) {
        this.value = validateAndRound(value);
    }

    public static RetencionPorcentaje of(BigDecimal value) {
        return new RetencionPorcentaje(value);
    }

    public static RetencionPorcentaje tenPercent() {
        return new RetencionPorcentaje(DEFAULT_VALUE);
    }

    public static RetencionPorcentaje zero() {
        return new RetencionPorcentaje(BigDecimal.ZERO);
    }

    private BigDecimal validateAndRound(BigDecimal val) {
        Objects.requireNonNull(val, "El porcentaje de retención no puede ser nulo");

        // Round to 2 decimals
        BigDecimal rounded = val.setScale(2, RoundingMode.HALF_UP);

        if (rounded.compareTo(MIN) < 0 || rounded.compareTo(MAX) > 0) {
            throw new IllegalArgumentException("El porcentaje de retención debe estar entre 0 y 100");
        }

        return rounded;
    }

    public BigDecimal getValue() {
        return value;
    }

    public MontoEstimado calcularRetencion(MontoEstimado monto) {
        BigDecimal factor = this.value.divide(new BigDecimal("100.00"), 4, RoundingMode.HALF_UP);
        return monto.multiplicar(factor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RetencionPorcentaje that = (RetencionPorcentaje) o;
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
