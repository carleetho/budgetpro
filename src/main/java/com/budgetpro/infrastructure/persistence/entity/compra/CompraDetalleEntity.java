package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un detalle de compra en la base de datos.
 * 
 * Mapea la tabla `compra_detalle` del ERD físico definitivo.
 * 
 * Alineado con el ERD físico definitivo:
 * - id UUID
 * - compra_id UUID
 * - recurso_id UUID
 * - cantidad NUMERIC(19,6)
 * - precio_unitario NUMERIC(19,4)
 * - created_at, updated_at
 */
@Entity
@Table(name = "compra_detalle",
       indexes = {
           @Index(name = "idx_compra_detalle_compra", columnList = "compra_id"),
           @Index(name = "idx_compra_detalle_recurso", columnList = "recurso_id")
       })
public class CompraDetalleEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false, updatable = false)
    private CompraEntity compra;

    @Column(name = "recurso_id", nullable = false, updatable = false)
    private UUID recursoId;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requiere constructor sin argumentos
    protected CompraDetalleEntity() {
    }

    /**
     * Constructor público para crear nuevas entidades.
     */
    public CompraDetalleEntity(UUID id, CompraEntity compra, UUID recursoId, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.id = id;
        this.compra = compra;
        this.recursoId = recursoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CompraEntity getCompra() {
        return compra;
    }

    public void setCompra(CompraEntity compra) {
        this.compra = compra;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public void setRecursoId(UUID recursoId) {
        this.recursoId = recursoId;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
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
