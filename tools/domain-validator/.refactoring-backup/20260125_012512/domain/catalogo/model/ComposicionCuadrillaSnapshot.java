package com.budgetpro.domain.catalogo.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa la composición de una cuadrilla.
 * 
 * Captura la cantidad y costo diario de cada tipo de personal en una cuadrilla.
 * Permite cálculo dinámico del costo día cuadrilla: Σ(cantidad × costoDia × tipoCambio)
 * 
 * Invariantes:
 * - personalExternalId no puede estar vacío
 * - personalNombre no puede estar vacío
 * - cantidad debe ser positiva (> 0)
 * - costoDia no puede ser negativo (≥ 0)
 * - moneda no puede estar vacía
 */
public record ComposicionCuadrillaSnapshot(
        String personalExternalId,
        String personalNombre,
        BigDecimal cantidad,
        BigDecimal costoDia,
        String moneda
) {
    public ComposicionCuadrillaSnapshot {
        validarInvariantes(personalExternalId, personalNombre, cantidad, costoDia, moneda);
    }

    private void validarInvariantes(String personalExternalId,
                                    String personalNombre,
                                    BigDecimal cantidad,
                                    BigDecimal costoDia,
                                    String moneda) {
        if (personalExternalId == null || personalExternalId.isBlank()) {
            throw new IllegalArgumentException("El personalExternalId no puede estar vacío");
        }
        if (personalNombre == null || personalNombre.isBlank()) {
            throw new IllegalArgumentException("El personalNombre no puede estar vacío");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        if (costoDia == null || costoDia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costoDia no puede ser negativo");
        }
        if (moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComposicionCuadrillaSnapshot that = (ComposicionCuadrillaSnapshot) o;
        return Objects.equals(personalExternalId, that.personalExternalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personalExternalId);
    }
}
