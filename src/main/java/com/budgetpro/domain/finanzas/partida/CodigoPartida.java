package com.budgetpro.domain.finanzas.partida;

import java.util.Objects;

/**
 * Value Object que encapsula el código único de una Partida (ej: "MAT-01").
 * Inmutable por diseño.
 * 
 * El código debe seguir un formato válido (no vacío, normalizado).
 */
public final class CodigoPartida {

    private final String value;

    private CodigoPartida(String value) {
        validarCodigo(value);
        this.value = normalizarCodigo(value);
    }

    /**
     * Crea un CodigoPartida desde un String.
     * Aplica normalización automática (trim, uppercase).
     * 
     * @param value El código de la partida (ej: "MAT-01", "MO-05")
     * @return Un CodigoPartida normalizado
     * @throws IllegalArgumentException si el código es null o vacío
     */
    public static CodigoPartida of(String value) {
        return new CodigoPartida(value);
    }

    private static void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la partida no puede ser nulo ni vacío");
        }
    }

    /**
     * Normaliza el código: trim + uppercase.
     * Ejemplo: "  mat-01 " -> "MAT-01"
     */
    private static String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

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
