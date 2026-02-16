package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla detalle_orden_compra.
 * 
 * Representa un detalle (línea) de una orden de compra.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "detalle_orden_compra",
       indexes = {
           @Index(name = "idx_detalle_orden_compra_orden", columnList = "orden_compra_id"),
           @Index(name = "idx_detalle_orden_compra_partida", columnList = "partida_id")
       })
public class DetalleOrdenCompraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false, updatable = false)
    private OrdenCompraEntity ordenCompra;

    @Column(name = "partida_id", nullable = false, updatable = false)
    private UUID partidaId;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "unidad", length = 20)
    private String unidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(name = "orden", nullable = false)
    private Integer orden; // Orden de visualización del detalle

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected DetalleOrdenCompraEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del detalle
     * @param ordenCompra OrdenCompraEntity asociada
     * @param partidaId ID de la partida presupuestaria
     * @param descripcion Descripción del ítem
     * @param cantidad Cantidad solicitada
     * @param unidad Unidad de medida
     * @param precioUnitario Precio unitario
     * @param subtotal Subtotal calculado
     * @param orden Orden de visualización
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public DetalleOrdenCompraEntity(UUID id, OrdenCompraEntity ordenCompra, UUID partidaId,
                                    String descripcion, BigDecimal cantidad, String unidad,
                                    BigDecimal precioUnitario, BigDecimal subtotal, Integer orden,
                                    Integer version) {
        this.id = id;
        this.ordenCompra = ordenCompra;
        this.partidaId = partidaId;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.orden = orden;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrdenCompraEntity getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(OrdenCompraEntity ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
