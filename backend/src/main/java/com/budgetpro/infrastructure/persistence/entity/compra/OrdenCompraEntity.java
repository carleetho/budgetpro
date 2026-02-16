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
 * Entidad JPA para la tabla orden_compra.
 * 
 * Representa una orden de compra con máquina de estados completa.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "orden_compra",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_orden_compra_numero", columnNames = "numero")
       },
       indexes = {
           @Index(name = "idx_orden_compra_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_orden_compra_estado", columnList = "estado"),
           @Index(name = "idx_orden_compra_proveedor", columnList = "proveedor_id"),
           @Index(name = "idx_orden_compra_fecha", columnList = "fecha")
       })
public class OrdenCompraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "numero", nullable = false, unique = true, length = 50)
    private String numero;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false, updatable = false)
    private ProveedorEntity proveedor;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado estado;

    @Column(name = "monto_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoTotal;

    @Column(name = "condiciones_pago", length = 500)
    private String condicionesPago;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<DetalleOrdenCompraEntity> detalles = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected OrdenCompraEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID de la orden de compra
     * @param numero Número secuencial de la orden
     * @param proyectoId ID del proyecto asociado
     * @param proveedor ProveedorEntity asociado
     * @param fecha Fecha de la orden
     * @param estado Estado de la orden
     * @param montoTotal Monto total calculado
     * @param condicionesPago Condiciones de pago (opcional)
     * @param observaciones Observaciones (opcional)
     * @param version Versión (puede ser null para nuevas entidades)
     * @param createdBy ID del usuario que crea la orden
     * @param updatedBy ID del usuario que actualiza la orden
     */
    public OrdenCompraEntity(UUID id, String numero, UUID proyectoId, ProveedorEntity proveedor,
                             LocalDate fecha, com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado estado,
                             BigDecimal montoTotal, String condicionesPago, String observaciones,
                             Integer version, UUID createdBy, UUID updatedBy) {
        this.id = id;
        this.numero = numero;
        this.proyectoId = proyectoId;
        this.proveedor = proveedor;
        this.fecha = fecha;
        this.estado = estado;
        this.montoTotal = montoTotal;
        this.condicionesPago = condicionesPago;
        this.observaciones = observaciones;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(UUID proyectoId) {
        this.proyectoId = proyectoId;
    }

    public ProveedorEntity getProveedor() {
        return proveedor;
    }

    public void setProveedor(ProveedorEntity proveedor) {
        this.proveedor = proveedor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado estado) {
        this.estado = estado;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getCondicionesPago() {
        return condicionesPago;
    }

    public void setCondicionesPago(String condicionesPago) {
        this.condicionesPago = condicionesPago;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<DetalleOrdenCompraEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleOrdenCompraEntity> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }
}
