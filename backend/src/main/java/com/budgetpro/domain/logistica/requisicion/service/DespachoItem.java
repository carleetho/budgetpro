package com.budgetpro.domain.logistica.requisicion.service;

import com.budgetpro.domain.logistica.requisicion.model.RequisicionItemId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * DTO para un ítem a despachar en una requisición.
 * Contiene el ID del RequisicionItem y la cantidad a despachar.
 */
public final class DespachoItem {

    private final RequisicionItemId requisicionItemId;
    private final BigDecimal cantidadADespachar;

    public DespachoItem(RequisicionItemId requisicionItemId, BigDecimal cantidadADespachar) {
        this.requisicionItemId = Objects.requireNonNull(requisicionItemId, "El requisicionItemId no puede ser nulo");
        if (cantidadADespachar == null || cantidadADespachar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a despachar debe ser positiva");
        }
        this.cantidadADespachar = cantidadADespachar;
    }

    public RequisicionItemId getRequisicionItemId() {
        return requisicionItemId;
    }

    public BigDecimal getCantidadADespachar() {
        return cantidadADespachar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DespachoItem that = (DespachoItem) o;
        return Objects.equals(requisicionItemId, that.requisicionItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requisicionItemId);
    }

    @Override
    public String toString() {
        return String.format("DespachoItem{requisicionItemId=%s, cantidadADespachar=%s}",
                           requisicionItemId, cantidadADespachar);
    }
}
