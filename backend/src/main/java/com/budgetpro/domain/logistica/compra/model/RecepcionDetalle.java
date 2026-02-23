package com.budgetpro.domain.logistica.compra.model;

import com.budgetpro.domain.logistica.almacen.model.AlmacenId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna del agregado Recepcion.
 * 
 * Representa un detalle de recepción de productos con soporte para distribución
 * multi-almacén.
 * 
 * Invariantes:
 * - El compraDetalleId no puede ser nulo
 * - El recursoId no puede ser nulo
 * - El almacenId no puede ser nulo
 * - La cantidadRecibida debe ser mayor a cero
 * - El precioUnitario debe ser mayor o igual a cero
 * 
 * Contexto: Logística & Compras - Recepción de Bienes
 */
public final class RecepcionDetalle {

    private final RecepcionDetalleId id;
    private final UUID compraDetalleId; // Referencia al CompraDetalle original
    private final UUID recursoId; // ID del recurso recibido
    private final AlmacenId almacenId; // Almacén donde se recibe (soporte multi-almacén)
    private final BigDecimal cantidadRecibida; // Cantidad recibida en esta recepción
    private final BigDecimal precioUnitario; // Precio unitario al momento de la recepción

    /**
     * Constructor privado. Usar factory methods.
     */
    private RecepcionDetalle(RecepcionDetalleId id, UUID compraDetalleId, UUID recursoId, AlmacenId almacenId,
            BigDecimal cantidadRecibida, BigDecimal precioUnitario) {
        this.id = Objects.requireNonNull(id, "El ID del detalle de recepción no puede ser nulo");
        this.compraDetalleId = Objects.requireNonNull(compraDetalleId, "El compraDetalleId no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El recursoId no puede ser nulo");
        this.almacenId = Objects.requireNonNull(almacenId, "El almacenId no puede ser nulo");
        this.cantidadRecibida = validarCantidad(cantidadRecibida);
        this.precioUnitario = validarPrecio(precioUnitario);
    }

    /**
     * Factory method para crear un nuevo RecepcionDetalle.
     * 
     * @param id el identificador único del detalle de recepción
     * @param compraDetalleId el ID del detalle de compra asociado
     * @param recursoId el ID del recurso recibido
     * @param almacenId el ID del almacén donde se recibe
     * @param cantidadRecibida la cantidad recibida (debe ser > 0)
     * @param precioUnitario el precio unitario (debe ser >= 0)
     * @return una nueva instancia de RecepcionDetalle
     */
    public static RecepcionDetalle crear(RecepcionDetalleId id, UUID compraDetalleId, UUID recursoId,
            AlmacenId almacenId, BigDecimal cantidadRecibida, BigDecimal precioUnitario) {
        return new RecepcionDetalle(id, compraDetalleId, recursoId, almacenId, cantidadRecibida, precioUnitario);
    }

    /**
     * Valida que la cantidad recibida sea mayor a cero.
     * 
     * @param cantidad la cantidad a validar
     * @return la cantidad validada
     * @throws IllegalArgumentException si la cantidad es nula, cero o negativa
     */
    private BigDecimal validarCantidad(BigDecimal cantidad) {
        if (cantidad == null) {
            throw new IllegalArgumentException("La cantidad recibida no puede ser nula");
        }
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        return cantidad;
    }

    /**
     * Valida que el precio unitario sea mayor o igual a cero.
     * 
     * @param precio el precio a validar
     * @return el precio validado
     * @throws IllegalArgumentException si el precio es nulo o negativo
     */
    private BigDecimal validarPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new IllegalArgumentException("El precio unitario no puede ser nulo");
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor o igual a cero");
        }
        return precio;
    }

    // Getters

    public RecepcionDetalleId getId() {
        return id;
    }

    public UUID getCompraDetalleId() {
        return compraDetalleId;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public AlmacenId getAlmacenId() {
        return almacenId;
    }

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RecepcionDetalle that = (RecepcionDetalle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "RecepcionDetalle{id=%s, compraDetalleId=%s, recursoId=%s, almacenId=%s, cantidadRecibida=%s, precioUnitario=%s}",
                id, compraDetalleId, recursoId, almacenId, cantidadRecibida, precioUnitario);
    }
}
