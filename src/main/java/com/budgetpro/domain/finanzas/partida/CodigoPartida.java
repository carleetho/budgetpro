package com.budgetpro.domain.finanzas.partida;

import java.util.Objects;

/**
 * Value Object que encapsula el código único de una Partida (ej: "MAT-01").
 * 
 * Normalización automática: Trim + UpperCase
 * Ejemplo: "  mat-01 " -> "MAT-01"
 * 
 * Inmutable por diseño.
 */
public final class CodigoPartida {

    private final String value;

    private CodigoPartida(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El código de la partida no puede ser nulo o vacío");
        }
        // Normalización: Trim + UpperCase
        this.value = value.trim().toUpperCase();
    }

    /**
     * Factory method para crear un CodigoPartida desde String.
     * Aplica normalización automática (Trim + UpperCase).
     */
    public static CodigoPartida of(String value) {
        return new CodigoPartida(value);
    }

    /**
     * Obtiene el valor normalizado del código.
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodigoPartida that = (CodigoPartida) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
