package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla compra.
 * 
 * Representa una compra realizada en el proyecto.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "compra",
       indexes = {
           @Index(name = "idx_compra_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_compra_fecha", columnList = "fecha"),
           @Index(name = "idx_compra_estado", columnList = "estado")
       })
// REGLA-117
// REGLA-116
// REGLA-115
public class CompraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "proveedor", nullable = false, length = 200)
    private String proveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private com.budgetpro.domain.logistica.compra.model.EstadoCompra estado;

    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CompraDetalleEntity> detalles = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected CompraEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID de la compra
     * @param proyectoId ID del proyecto asociado
     * @param fecha Fecha de la compra
     * @param proveedor Nombre del proveedor
     * @param estado Estado de la compra
     * @param total Total de la compra
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public CompraEntity(UUID id, UUID proyectoId, LocalDate fecha, String proveedor,
                       com.budgetpro.domain.logistica.compra.model.EstadoCompra estado,
                       BigDecimal total, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.fecha = fecha;
        this.proveedor = proveedor;
        this.estado = estado;
        this.total = total;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(UUID proyectoId) {
        this.proyectoId = proyectoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public com.budgetpro.domain.logistica.compra.model.EstadoCompra getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.logistica.compra.model.EstadoCompra estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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

    public List<CompraDetalleEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<CompraDetalleEntity> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }
}
