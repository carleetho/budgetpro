package com.budgetpro.domain.finanzas.estimacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa un monto estimado. Mantiene 4 decimales
 * internamente para cálculos precisos, pero se puede obtener redondeado a 2
 * decimales para persistencia/visualización.
 */
public final class MontoEstimado {

    private final BigDecimal value;

    private MontoEstimado(BigDecimal value) {
        this.value = validateAndRound(value);
    }

    public static MontoEstimado of(BigDecimal value) {
        return new MontoEstimado(value);
    }

    public static MontoEstimado zero() {
        return new MontoEstimado(BigDecimal.ZERO);
    }

    private BigDecimal validateAndRound(BigDecimal val) {
        Objects.requireNonNull(val, "El monto estimado no puede ser nulo");
        if (val.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto estimado no puede ser negativo");
        }
        // Use 4 decimals for internal precision
        return val.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getValue() {
        return value;
    }

    public BigDecimal getValueForPersistence() {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public MontoEstimado sumar(MontoEstimado other) {
        return new MontoEstimado(this.value.add(other.value));
    }

    public MontoEstimado restar(MontoEstimado other) {
        return new MontoEstimado(this.value.subtract(other.value));
    }

    public MontoEstimado multiplicar(BigDecimal factor) {
        return new MontoEstimado(this.value.multiply(factor));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MontoEstimado that = (MontoEstimado) o;
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
