package com.budgetpro.domain.finanzas.evm.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa el identificador único de una serie temporal de EVM.
 * 
 * Inmutable por diseño, encapsula un UUID para identificar unívocamente
 * un registro en la serie temporal de métricas EVM.
 */
public final class EVMTimeSeriesId {

    private final UUID value;

    private EVMTimeSeriesId(UUID value) {
        this.value = Objects.requireNonNull(value, "El ID de la serie temporal de EVM no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo EVMTimeSeriesId.
     * 
     * @return Un nuevo EVMTimeSeriesId con un UUID generado aleatoriamente
     */
    public static EVMTimeSeriesId nuevo() {
        return new EVMTimeSeriesId(UUID.randomUUID());
    }

    /**
     * Factory method para crear un EVMTimeSeriesId desde un UUID existente.
     * 
     * @param value El UUID a encapsular
     * @return Un EVMTimeSeriesId que envuelve el UUID proporcionado
     * @throws NullPointerException si el UUID es nulo
     */
    public static EVMTimeSeriesId de(UUID value) {
        return new EVMTimeSeriesId(value);
    }

    /**
     * Obtiene el valor UUID encapsulado.
     * 
     * @return El UUID del identificador
     */
    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EVMTimeSeriesId that = (EVMTimeSeriesId) o;
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
