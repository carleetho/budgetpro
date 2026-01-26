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
           @Index(name = "idx_compra_detalle_recurso_external", columnList = "recurso_external_id"),
           @Index(name = "idx_compra_detalle_partida", columnList = "partida_id")
       })
public class CompraDetalleEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false, updatable = false)
    private CompraEntity compra;

    @Column(name = "recurso_external_id", nullable = false, length = 255, updatable = false)
    private String recursoExternalId; // Referencia externa al recurso (ej. "MAT-001")

    @Column(name = "recurso_nombre", nullable = false, length = 500, updatable = false)
    private String recursoNombre; // Snapshot del nombre del recurso para display/reporting

    @Column(name = "unidad", length = 20, updatable = false)
    private String unidad; // Unidad en que llega la compra (Authority by PO). Null = usar catálogo.

    @Column(name = "partida_id", updatable = false)
    private UUID partidaId; // Puede ser null si no aplica

    @Enumerated(EnumType.STRING)
    @Column(name = "naturaleza_gasto", nullable = false, length = 30)
    private com.budgetpro.domain.logistica.compra.model.NaturalezaGasto naturalezaGasto;

    @Enumerated(EnumType.STRING)
    @Column(name = "relacion_contractual", nullable = false, length = 30)
    private com.budgetpro.domain.logistica.compra.model.RelacionContractual relacionContractual;

    @Enumerated(EnumType.STRING)
    @Column(name = "rubro_insumo", nullable = false, length = 50)
    private com.budgetpro.domain.logistica.compra.model.RubroInsumo rubroInsumo;

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
     * @param recursoExternalId ID externo del recurso (ej. "MAT-001")
     * @param recursoNombre Nombre del recurso (snapshot)
     * @param unidad Unidad en que llega la compra (Authority by PO). Null = usar catálogo.
     * @param partidaId ID de la partida (imputación presupuestal)
     * @param cantidad Cantidad comprada
     * @param precioUnitario Precio unitario
     * @param subtotal Subtotal calculado
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public CompraDetalleEntity(UUID id, CompraEntity compra,
                               String recursoExternalId,
                               String recursoNombre,
                               String unidad,
                               UUID partidaId,
                               com.budgetpro.domain.logistica.compra.model.NaturalezaGasto naturalezaGasto,
                               com.budgetpro.domain.logistica.compra.model.RelacionContractual relacionContractual,
                               com.budgetpro.domain.logistica.compra.model.RubroInsumo rubroInsumo,
                               BigDecimal cantidad, BigDecimal precioUnitario,
                               BigDecimal subtotal, Integer version) {
        this.id = id;
        this.compra = compra;
        this.recursoExternalId = recursoExternalId;
        this.recursoNombre = recursoNombre;
        this.unidad = unidad;
        this.partidaId = partidaId;
        this.naturalezaGasto = naturalezaGasto;
        this.relacionContractual = relacionContractual;
        this.rubroInsumo = rubroInsumo;
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

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public void setRecursoExternalId(String recursoExternalId) {
        this.recursoExternalId = recursoExternalId;
    }

    public String getRecursoNombre() {
        return recursoNombre;
    }

    public void setRecursoNombre(String recursoNombre) {
        this.recursoNombre = recursoNombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public com.budgetpro.domain.logistica.compra.model.NaturalezaGasto getNaturalezaGasto() {
        return naturalezaGasto;
    }

    public void setNaturalezaGasto(com.budgetpro.domain.logistica.compra.model.NaturalezaGasto naturalezaGasto) {
        this.naturalezaGasto = naturalezaGasto;
    }

    public com.budgetpro.domain.logistica.compra.model.RelacionContractual getRelacionContractual() {
        return relacionContractual;
    }

    public void setRelacionContractual(com.budgetpro.domain.logistica.compra.model.RelacionContractual relacionContractual) {
        this.relacionContractual = relacionContractual;
    }

    public com.budgetpro.domain.logistica.compra.model.RubroInsumo getRubroInsumo() {
        return rubroInsumo;
    }

    public void setRubroInsumo(com.budgetpro.domain.logistica.compra.model.RubroInsumo rubroInsumo) {
        this.rubroInsumo = rubroInsumo;
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
