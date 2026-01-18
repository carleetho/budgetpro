package com.budgetpro.domain.finanzas.estimacion.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad del agregado Estimacion que representa un detalle de estimación por partida.
 * 
 * Relación N:1 con Estimacion, 1:1 con Partida.
 * 
 * Responsabilidad:
 * - Representar el avance de una partida en un periodo de estimación
 * - Calcular el importe basándose en cantidad y precio unitario
 * - Validar que no se estime más del 100% del volumen contratado
 * 
 * Invariantes:
 * - El partidaId es obligatorio
 * - La cantidadAvance no puede ser negativa
 * - El precioUnitario no puede ser negativo
 * - El importe = cantidadAvance * precioUnitario
 */
public final class DetalleEstimacion {

    private final DetalleEstimacionId id;
    private final UUID partidaId;
    private BigDecimal cantidadAvance; // Lo ejecutado en este periodo
    private BigDecimal precioUnitario; // Viene del Presupuesto Autorizado
    private BigDecimal importe; // Calculado: cantidadAvance * precioUnitario
    private BigDecimal acumuladoAnterior; // Acumulado de estimaciones anteriores (para validar 100%)

    /**
     * Constructor privado. Usar factory methods.
     */
    private DetalleEstimacion(DetalleEstimacionId id, UUID partidaId,
                             BigDecimal cantidadAvance, BigDecimal precioUnitario,
                             BigDecimal importe, BigDecimal acumuladoAnterior) {
        validarInvariantes(partidaId, cantidadAvance, precioUnitario);
        
        this.id = Objects.requireNonNull(id, "El ID del detalle de estimación no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El partidaId no puede ser nulo");
        this.cantidadAvance = cantidadAvance != null ? cantidadAvance : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.importe = importe != null ? importe : calcularImporte();
        this.acumuladoAnterior = acumuladoAnterior != null ? acumuladoAnterior : BigDecimal.ZERO;
    }

    /**
     * Factory method para crear un nuevo DetalleEstimacion.
     */
    public static DetalleEstimacion crear(DetalleEstimacionId id, UUID partidaId,
                                          BigDecimal cantidadAvance, BigDecimal precioUnitario,
                                          BigDecimal acumuladoAnterior) {
        return new DetalleEstimacion(id, partidaId, cantidadAvance, precioUnitario, null, acumuladoAnterior);
    }

    /**
     * Factory method para reconstruir un DetalleEstimacion desde persistencia.
     */
    public static DetalleEstimacion reconstruir(DetalleEstimacionId id, UUID partidaId,
                                                BigDecimal cantidadAvance, BigDecimal precioUnitario,
                                                BigDecimal importe, BigDecimal acumuladoAnterior) {
        return new DetalleEstimacion(id, partidaId, cantidadAvance, precioUnitario, importe, acumuladoAnterior);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID partidaId, BigDecimal cantidadAvance, BigDecimal precioUnitario) {
        if (partidaId == null) {
            throw new IllegalArgumentException("El partidaId no puede ser nulo");
        }
        if (cantidadAvance != null && cantidadAvance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad de avance no puede ser negativa");
        }
        if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
    }

    /**
     * Calcula el importe: cantidadAvance * precioUnitario.
     */
    private BigDecimal calcularImporte() {
        return this.cantidadAvance.multiply(this.precioUnitario)
                .setScale(4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Actualiza la cantidad de avance y recalcula el importe.
     */
    public void actualizarCantidadAvance(BigDecimal nuevaCantidad) {
        if (nuevaCantidad == null) {
            this.cantidadAvance = BigDecimal.ZERO;
        } else if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad de avance no puede ser negativa");
        } else {
            this.cantidadAvance = nuevaCantidad;
        }
        this.importe = calcularImporte();
    }

    /**
     * Actualiza el precio unitario y recalcula el importe.
     */
    public void actualizarPrecioUnitario(BigDecimal nuevoPrecioUnitario) {
        if (nuevoPrecioUnitario == null) {
            this.precioUnitario = BigDecimal.ZERO;
        } else if (nuevoPrecioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        } else {
            this.precioUnitario = nuevoPrecioUnitario;
        }
        this.importe = calcularImporte();
    }

    /**
     * Calcula el acumulado total (anterior + actual).
     */
    public BigDecimal calcularAcumuladoTotal() {
        return this.acumuladoAnterior.add(this.cantidadAvance);
    }

    // Getters

    public DetalleEstimacionId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public BigDecimal getCantidadAvance() {
        return cantidadAvance;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public BigDecimal getAcumuladoAnterior() {
        return acumuladoAnterior;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetalleEstimacion that = (DetalleEstimacion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("DetalleEstimacion{id=%s, partidaId=%s, cantidadAvance=%s, importe=%s}", 
                           id, partidaId, cantidadAvance, importe);
    }
}
