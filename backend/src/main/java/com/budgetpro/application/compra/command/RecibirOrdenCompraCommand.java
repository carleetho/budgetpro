package com.budgetpro.application.compra.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando inmutable para recibir una orden de compra.
 * 
 * Representa el payload de la API para registrar una recepción de productos
 * con su guía de remisión y detalles de recepción.
 */
public final class RecibirOrdenCompraCommand {

    private final UUID compraId;
    private final LocalDate fechaRecepcion;
    private final String guiaRemision;
    private final List<DetalleCommand> detalles;
    private final UUID usuarioId;

    /**
     * Constructor con validación.
     * 
     * @param compraId ID de la compra a recibir
     * @param fechaRecepcion Fecha en que se recibió la mercancía
     * @param guiaRemision Número de guía de remisión (requisito legal)
     * @param detalles Lista de detalles de recepción
     * @param usuarioId ID del usuario que realiza la recepción
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public RecibirOrdenCompraCommand(
            @NotNull(message = "El ID de la compra es obligatorio") UUID compraId,
            @NotNull(message = "La fecha de recepción es obligatoria") LocalDate fechaRecepcion,
            @NotBlank(message = "La guía de remisión es obligatoria") String guiaRemision,
            @NotEmpty(message = "La lista de detalles no puede estar vacía") @Valid List<DetalleCommand> detalles,
            @NotNull(message = "El ID del usuario es obligatorio") UUID usuarioId) {
        
        this.compraId = Objects.requireNonNull(compraId, "El ID de la compra no puede ser nulo");
        this.fechaRecepcion = Objects.requireNonNull(fechaRecepcion, "La fecha de recepción no puede ser nula");
        this.guiaRemision = validarGuiaRemision(guiaRemision);
        this.detalles = validarDetalles(detalles);
        this.usuarioId = Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser nulo");
    }

    /**
     * Valida que la guía de remisión no sea nula ni esté en blanco.
     */
    private String validarGuiaRemision(String guiaRemision) {
        if (guiaRemision == null || guiaRemision.isBlank()) {
            throw new IllegalArgumentException("La guía de remisión es obligatoria");
        }
        return guiaRemision.trim();
    }

    /**
     * Valida que la lista de detalles no sea nula ni vacía, y crea una copia defensiva.
     */
    private List<DetalleCommand> validarDetalles(List<DetalleCommand> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La lista de detalles no puede estar vacía");
        }
        // Crear copia defensiva para evitar mutaciones externas
        return Collections.unmodifiableList(new ArrayList<>(detalles));
    }

    // Getters

    public UUID getCompraId() {
        return compraId;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public String getGuiaRemision() {
        return guiaRemision;
    }

    /**
     * Obtiene la lista de detalles (inmutable).
     */
    public List<DetalleCommand> getDetalles() {
        return detalles; // Ya es inmutable por Collections.unmodifiableList
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    /**
     * Clase interna inmutable que representa un detalle de recepción.
     */
    public static final class DetalleCommand {

        private final UUID detalleOrdenId;
        private final BigDecimal cantidadRecibida;
        private final UUID almacenId;

        /**
         * Constructor con validación.
         * 
         * @param detalleOrdenId ID del detalle de orden de compra
         * @param cantidadRecibida Cantidad recibida (debe ser > 0)
         * @param almacenId ID del almacén donde se recibe
         * @throws IllegalArgumentException si algún parámetro es inválido
         */
        public DetalleCommand(
                @NotNull(message = "El ID del detalle de orden es obligatorio") UUID detalleOrdenId,
                @NotNull(message = "La cantidad recibida es obligatoria") BigDecimal cantidadRecibida,
                @NotNull(message = "El ID del almacén es obligatorio") UUID almacenId) {
            
            this.detalleOrdenId = Objects.requireNonNull(detalleOrdenId, "El ID del detalle de orden no puede ser nulo");
            this.cantidadRecibida = validarCantidad(cantidadRecibida);
            this.almacenId = Objects.requireNonNull(almacenId, "El ID del almacén no puede ser nulo");
        }

        /**
         * Valida que la cantidad recibida sea mayor a cero.
         */
        private BigDecimal validarCantidad(BigDecimal cantidadRecibida) {
            if (cantidadRecibida == null) {
                throw new IllegalArgumentException("La cantidad recibida no puede ser nula");
            }
            if (cantidadRecibida.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad recibida debe ser mayor a cero");
            }
            return cantidadRecibida;
        }

        // Getters

        public UUID getDetalleOrdenId() {
            return detalleOrdenId;
        }

        public BigDecimal getCantidadRecibida() {
            return cantidadRecibida;
        }

        public UUID getAlmacenId() {
            return almacenId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DetalleCommand that = (DetalleCommand) o;
            return Objects.equals(detalleOrdenId, that.detalleOrdenId) &&
                   Objects.equals(cantidadRecibida, that.cantidadRecibida) &&
                   Objects.equals(almacenId, that.almacenId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(detalleOrdenId, cantidadRecibida, almacenId);
        }

        @Override
        public String toString() {
            return String.format("DetalleCommand{detalleOrdenId=%s, cantidadRecibida=%s, almacenId=%s}",
                    detalleOrdenId, cantidadRecibida, almacenId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecibirOrdenCompraCommand that = (RecibirOrdenCompraCommand) o;
        return Objects.equals(compraId, that.compraId) &&
               Objects.equals(fechaRecepcion, that.fechaRecepcion) &&
               Objects.equals(guiaRemision, that.guiaRemision) &&
               Objects.equals(detalles, that.detalles) &&
               Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compraId, fechaRecepcion, guiaRemision, detalles, usuarioId);
    }

    @Override
    public String toString() {
        return String.format("RecibirOrdenCompraCommand{compraId=%s, fechaRecepcion=%s, guiaRemision='%s', detalles=%d, usuarioId=%s}",
                compraId, fechaRecepcion, guiaRemision, detalles.size(), usuarioId);
    }
}
