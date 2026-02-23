package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA para la tabla recepcion_detalle.
 * 
 * Representa un detalle de recepción de productos asociado a una recepción,
 * vinculado a un detalle de compra, recurso y almacén específicos.
 */
@Entity
@Table(name = "recepcion_detalle")
public class RecepcionDetalleEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recepcion_id", nullable = false, updatable = false)
    private RecepcionEntity recepcion;

    @Column(name = "compra_detalle_id", nullable = false, updatable = false)
    private UUID compraDetalleId;

    @Column(name = "recurso_id", nullable = false, updatable = false)
    private UUID recursoId;

    @Column(name = "almacen_id", nullable = false, updatable = false)
    private UUID almacenId;

    @Column(name = "cantidad_recibida", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidadRecibida;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "movimiento_almacen_id")
    private UUID movimientoAlmacenId;

    /**
     * Constructor protegido para JPA.
     */
    protected RecepcionDetalleEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del detalle de recepción
     * @param recepcion RecepciónEntity asociada
     * @param compraDetalleId ID del detalle de compra
     * @param recursoId ID del recurso
     * @param almacenId ID del almacén
     * @param cantidadRecibida Cantidad recibida
     * @param precioUnitario Precio unitario
     * @param movimientoAlmacenId ID del movimiento de almacén creado
     */
    public RecepcionDetalleEntity(UUID id, RecepcionEntity recepcion, UUID compraDetalleId,
                                  UUID recursoId, UUID almacenId, BigDecimal cantidadRecibida,
                                  BigDecimal precioUnitario, UUID movimientoAlmacenId) {
        this.id = id;
        this.recepcion = recepcion;
        this.compraDetalleId = compraDetalleId;
        this.recursoId = recursoId;
        this.almacenId = almacenId;
        this.cantidadRecibida = cantidadRecibida;
        this.precioUnitario = precioUnitario;
        this.movimientoAlmacenId = movimientoAlmacenId;
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RecepcionEntity getRecepcion() {
        return recepcion;
    }

    public void setRecepcion(RecepcionEntity recepcion) {
        this.recepcion = recepcion;
    }

    public UUID getCompraDetalleId() {
        return compraDetalleId;
    }

    public void setCompraDetalleId(UUID compraDetalleId) {
        this.compraDetalleId = compraDetalleId;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public void setRecursoId(UUID recursoId) {
        this.recursoId = recursoId;
    }

    public UUID getAlmacenId() {
        return almacenId;
    }

    public void setAlmacenId(UUID almacenId) {
        this.almacenId = almacenId;
    }

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(BigDecimal cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public UUID getMovimientoAlmacenId() {
        return movimientoAlmacenId;
    }

    public void setMovimientoAlmacenId(UUID movimientoAlmacenId) {
        this.movimientoAlmacenId = movimientoAlmacenId;
    }
}
