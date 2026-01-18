package com.budgetpro.infrastructure.persistence.entity.apu;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla apu_insumo.
 * 
 * Representa un insumo (recurso) que forma parte de un APU.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "apu_insumo",
       indexes = {
           @Index(name = "idx_apu_insumo_apu", columnList = "apu_id"),
           @Index(name = "idx_apu_insumo_recurso", columnList = "recurso_id")
       })
public class ApuInsumoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apu_id", nullable = false, updatable = false)
    private ApuEntity apu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false, updatable = false)
    private com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad; // Cantidad técnica por unidad de partida

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario; // Snapshot del precio del recurso al momento de agregar

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal; // Calculado: cantidad * precioUnitario

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
    protected ApuInsumoEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del ApuInsumo
     * @param apu ApuEntity asociado
     * @param recurso RecursoEntity asociado
     * @param cantidad Cantidad técnica
     * @param precioUnitario Precio unitario (snapshot)
     * @param subtotal Subtotal calculado
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public ApuInsumoEntity(UUID id, ApuEntity apu, com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso,
                          BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal, Integer version) {
        this.id = id;
        this.apu = apu;
        this.recurso = recurso;
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

    public ApuEntity getApu() {
        return apu;
    }

    public void setApu(ApuEntity apu) {
        this.apu = apu;
    }

    public com.budgetpro.infrastructure.persistence.entity.RecursoEntity getRecurso() {
        return recurso;
    }

    public void setRecurso(com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso) {
        this.recurso = recurso;
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
