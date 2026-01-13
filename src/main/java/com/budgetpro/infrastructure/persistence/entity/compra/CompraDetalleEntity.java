package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla compra_detalle.
 * 
 * Representa un ítem comprado que está asociado a una partida específica.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "compra_detalle",
       indexes = {
           @Index(name = "idx_compra_detalle_compra", columnList = "compra_id"),
           @Index(name = "idx_compra_detalle_recurso", columnList = "recurso_id"),
           @Index(name = "idx_compra_detalle_partida", columnList = "partida_id")
       })
public class CompraDetalleEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false, updatable = false)
    private CompraEntity compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false, updatable = false)
    private com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso;

    @Column(name = "partida_id", nullable = false, updatable = false)
    private UUID partidaId; // CRÍTICO: Imputación presupuestal

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

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
    protected CompraDetalleEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del detalle
     * @param compra CompraEntity asociada
     * @param recurso RecursoEntity asociado
     * @param partidaId ID de la partida (imputación presupuestal)
     * @param cantidad Cantidad comprada
     * @param precioUnitario Precio unitario
     * @param subtotal Subtotal calculado
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public CompraDetalleEntity(UUID id, CompraEntity compra, 
                               com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso,
                               UUID partidaId, BigDecimal cantidad, BigDecimal precioUnitario,
                               BigDecimal subtotal, Integer version) {
        this.id = id;
        this.compra = compra;
        this.recurso = recurso;
        this.partidaId = partidaId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
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

    public com.budgetpro.infrastructure.persistence.entity.RecursoEntity getRecurso() {
        return recurso;
    }

    public void setRecurso(com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso) {
        this.recurso = recurso;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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
