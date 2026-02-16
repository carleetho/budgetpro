package com.budgetpro.domain.logistica.compra.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa un detalle de orden de compra.
 * 
 * Representa un ítem de la orden de compra asociado a una partida presupuestaria.
 * 
 * Invariantes:
 * - La descripción no puede ser nula ni estar en blanco
 * - La cantidad debe ser mayor que cero
 * - El precio unitario no puede ser negativo
 * - El subtotal = cantidad * precioUnitario (redondeado a 2 decimales)
 * - La partidaId debe ser una partida leaf válida (REGLA-153)
 */
public final class DetalleOrdenCompra {

    private final UUID partidaId;
    private final String descripcion;
    private final BigDecimal cantidad;
    private final String unidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal; // Calculado: cantidad * precioUnitario

    /**
     * Constructor privado. Usar factory methods.
     */
    private DetalleOrdenCompra(UUID partidaId, String descripcion, BigDecimal cantidad, String unidad,
                               BigDecimal precioUnitario, BigDecimal subtotal) {
        validarInvariantes(partidaId, descripcion, cantidad, precioUnitario);

        this.partidaId = Objects.requireNonNull(partidaId, "La partidaId no puede ser nula");
        this.descripcion = normalizarDescripcion(descripcion);
        this.cantidad = cantidad;
        this.unidad = unidad != null && !unidad.isBlank() ? unidad.trim() : null;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal : calcularSubtotal();
    }

    /**
     * Factory method para crear un nuevo DetalleOrdenCompra.
     */
    public static DetalleOrdenCompra crear(UUID partidaId, String descripcion, BigDecimal cantidad,
                                          String unidad, BigDecimal precioUnitario) {
        return new DetalleOrdenCompra(partidaId, descripcion, cantidad, unidad, precioUnitario, null);
    }

    /**
     * Factory method para reconstruir un DetalleOrdenCompra desde persistencia.
     */
    public static DetalleOrdenCompra reconstruir(UUID partidaId, String descripcion, BigDecimal cantidad,
                                                 String unidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        return new DetalleOrdenCompra(partidaId, descripcion, cantidad, unidad, precioUnitario, subtotal);
    }

    /**
     * Valida las invariantes del detalle.
     */
    private void validarInvariantes(UUID partidaId, String descripcion, BigDecimal cantidad,
                                   BigDecimal precioUnitario) {
        if (partidaId == null) {
            throw new IllegalArgumentException("La partidaId no puede ser nula");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
    }

    /**
     * Normaliza la descripción (trim).
     */
    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        return descripcion.trim();
    }

    /**
     * Calcula el subtotal: cantidad * precioUnitario (redondeado a 2 decimales con HALF_UP).
     */
    private BigDecimal calcularSubtotal() {
        return cantidad.multiply(precioUnitario)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Getters

    public UUID getPartidaId() {
        return partidaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
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
        DetalleOrdenCompra that = (DetalleOrdenCompra) o;
        return Objects.equals(partidaId, that.partidaId) &&
               Objects.equals(descripcion, that.descripcion) &&
               Objects.equals(cantidad, that.cantidad) &&
               Objects.equals(precioUnitario, that.precioUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partidaId, descripcion, cantidad, precioUnitario);
    }

    @Override
    public String toString() {
        return String.format("DetalleOrdenCompra{partidaId=%s, descripcion='%s', cantidad=%s, unidad='%s', precioUnitario=%s, subtotal=%s}",
                partidaId, descripcion, cantidad, unidad, precioUnitario, subtotal);
    }
}
