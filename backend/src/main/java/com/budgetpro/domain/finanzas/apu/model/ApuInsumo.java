package com.budgetpro.domain.finanzas.apu.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna del agregado APU.
 * 
 * Representa un insumo (recurso) que forma parte del análisis de precios unitarios.
 * 
 * Invariantes:
 * - El recursoId no puede ser nulo
 * - La cantidad no puede ser negativa
 * - El precioUnitario no puede ser negativo
 * - El subtotal = cantidad * precioUnitario
 */
public final class ApuInsumo {

    private final ApuInsumoId id;
    private final UUID recursoId;
    // nosemgrep
    private BigDecimal cantidad; // Cantidad técnica por unidad de partida
    // nosemgrep
    private BigDecimal precioUnitario; // Snapshot del precio del recurso al momento de agregar
    // nosemgrep
    private BigDecimal subtotal; // Calculado: cantidad * precioUnitario

    /**
     * Constructor privado. Usar factory methods.
     */
    private ApuInsumo(ApuInsumoId id, UUID recursoId, BigDecimal cantidad, BigDecimal precioUnitario) {
        validarInvariantes(recursoId, cantidad, precioUnitario);
        
        this.id = Objects.requireNonNull(id, "El ID del ApuInsumo no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El recursoId no puede ser nulo");
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = calcularSubtotal();
    }

    /**
     * Factory method para crear un nuevo ApuInsumo.
     */
    public static ApuInsumo crear(ApuInsumoId id, UUID recursoId, BigDecimal cantidad, BigDecimal precioUnitario) {
        return new ApuInsumo(id, recursoId, cantidad, precioUnitario);
    }

    /**
     * Factory method para reconstruir un ApuInsumo desde persistencia.
     */
    public static ApuInsumo reconstruir(ApuInsumoId id, UUID recursoId, BigDecimal cantidad, 
                                       BigDecimal precioUnitario, BigDecimal subtotal) {
        ApuInsumo insumo = new ApuInsumo(id, recursoId, cantidad, precioUnitario);
        insumo.subtotal = subtotal != null ? subtotal : insumo.calcularSubtotal();
        return insumo;
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID recursoId, BigDecimal cantidad, BigDecimal precioUnitario) {
        if (recursoId == null) {
            throw new IllegalArgumentException("El recursoId no puede ser nulo");
        }
        if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
    }

    /**
     * Calcula el subtotal: cantidad * precioUnitario.
     */
    private BigDecimal calcularSubtotal() {
        return this.cantidad.multiply(this.precioUnitario);
    }

    /**
     * Actualiza la cantidad y recalcula el subtotal.
     */
    public void actualizarCantidad(BigDecimal nuevaCantidad) {
        if (nuevaCantidad == null) {
            this.cantidad = BigDecimal.ZERO;
        } else if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        } else {
            this.cantidad = nuevaCantidad;
        }
        this.subtotal = calcularSubtotal();
    }

    /**
     * Actualiza el precio unitario y recalcula el subtotal.
     */
    public void actualizarPrecioUnitario(BigDecimal nuevoPrecioUnitario) {
        if (nuevoPrecioUnitario == null) {
            this.precioUnitario = BigDecimal.ZERO;
        } else if (nuevoPrecioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        } else {
            this.precioUnitario = nuevoPrecioUnitario;
        }
        this.subtotal = calcularSubtotal();
    }

    // Getters

    public ApuInsumoId getId() {
        return id;
    }

    public UUID getRecursoId() {
        return recursoId;
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
        ApuInsumo that = (ApuInsumo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ApuInsumo{id=%s, recursoId=%s, cantidad=%s, precioUnitario=%s, subtotal=%s}", 
                           id, recursoId, cantidad, precioUnitario, subtotal);
    }
}
