package com.budgetpro.domain.catalogo.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad interna del agregado APUSnapshot.
 *
 * Representa un insumo snapshot con referencia externa al recurso.
 *
 * Invariantes:
 * - recursoExternalId no puede estar vacío
 * - recursoNombre no puede estar vacío
 * - cantidad no puede ser negativa
 * - precioUnitario no puede ser negativo
 * - subtotal = cantidad * precioUnitario
 */
public final class APUInsumoSnapshot {

    private final APUInsumoSnapshotId id;
    private final String recursoExternalId;
    private final String recursoNombre;
    private final BigDecimal cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    private APUInsumoSnapshot(APUInsumoSnapshotId id,
                              String recursoExternalId,
                              String recursoNombre,
                              BigDecimal cantidad,
                              BigDecimal precioUnitario,
                              BigDecimal subtotal) {
        validarInvariantes(recursoExternalId, recursoNombre, cantidad, precioUnitario);

        this.id = Objects.requireNonNull(id, "El ID del insumo snapshot no puede ser nulo");
        this.recursoExternalId = recursoExternalId.trim();
        this.recursoNombre = recursoNombre.trim();
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal : calcularSubtotal(this.cantidad, this.precioUnitario);
    }

    public static APUInsumoSnapshot crear(APUInsumoSnapshotId id,
                                          String recursoExternalId,
                                          String recursoNombre,
                                          BigDecimal cantidad,
                                          BigDecimal precioUnitario) {
        return new APUInsumoSnapshot(id, recursoExternalId, recursoNombre, cantidad, precioUnitario, null);
    }

    public static APUInsumoSnapshot reconstruir(APUInsumoSnapshotId id,
                                               String recursoExternalId,
                                               String recursoNombre,
                                               BigDecimal cantidad,
                                               BigDecimal precioUnitario,
                                               BigDecimal subtotal) {
        return new APUInsumoSnapshot(id, recursoExternalId, recursoNombre, cantidad, precioUnitario, subtotal);
    }

    private void validarInvariantes(String recursoExternalId,
                                    String recursoNombre,
                                    BigDecimal cantidad,
                                    BigDecimal precioUnitario) {
        if (recursoExternalId == null || recursoExternalId.isBlank()) {
            throw new IllegalArgumentException("El recursoExternalId no puede estar vacío");
        }
        if (recursoNombre == null || recursoNombre.isBlank()) {
            throw new IllegalArgumentException("El recursoNombre no puede estar vacío");
        }
        if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precioUnitario no puede ser negativo");
        }
    }

    private BigDecimal calcularSubtotal(BigDecimal cantidad, BigDecimal precioUnitario) {
        return cantidad.multiply(precioUnitario);
    }

    public APUInsumoSnapshotId getId() {
        return id;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public String getRecursoNombre() {
        return recursoNombre;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APUInsumoSnapshot that = (APUInsumoSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("APUInsumoSnapshot{id=%s, recursoExternalId='%s', cantidad=%s, precioUnitario=%s, subtotal=%s}",
                id, recursoExternalId, cantidad, precioUnitario, subtotal);
    }
}
