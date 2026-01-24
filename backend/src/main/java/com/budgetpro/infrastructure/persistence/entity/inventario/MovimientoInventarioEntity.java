package com.budgetpro.infrastructure.persistence.entity.inventario;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla movimiento_inventario (Kardex).
 * 
 * Representa un movimiento inmutable de entrada o salida de material.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "movimiento_inventario",
       indexes = {
           @Index(name = "idx_movimiento_inventario_item", columnList = "inventario_item_id"),
           @Index(name = "idx_movimiento_inventario_tipo", columnList = "tipo"),
           @Index(name = "idx_movimiento_inventario_fecha", columnList = "fecha_hora"),
           @Index(name = "idx_movimiento_inventario_compra", columnList = "compra_detalle_id"),
           @Index(name = "idx_movimiento_inventario_requisicion", columnList = "requisicion_id"),
           @Index(name = "idx_movimiento_inventario_transferencia", columnList = "transferencia_id"),
           @Index(name = "idx_movimiento_inventario_partida", columnList = "partida_id")
       })
public class MovimientoInventarioEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventario_item_id", nullable = false, updatable = false)
    private InventarioItemEntity inventarioItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private com.budgetpro.domain.logistica.inventario.model.TipoMovimientoInventario tipo;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoUnitario;

    @Column(name = "costo_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoTotal;

    @Column(name = "compra_detalle_id")
    private UUID compraDetalleId; // Opcional: para trazabilidad de compras

    @Column(name = "requisicion_id")
    private UUID requisicionId; // Opcional: ID de la requisición (solo para SALIDA_CONSUMO)

    @Column(name = "requisicion_item_id")
    private UUID requisicionItemId; // Opcional: ID del ítem de requisición (solo para SALIDA_CONSUMO)

    @Column(name = "partida_id")
    private UUID partidaId; // Opcional: Partida presupuestal (imputación AC)

    @Column(name = "transferencia_id")
    private UUID transferenciaId; // Opcional: ID para vincular transferencias

    @Column(name = "actividad_id")
    private UUID actividadId; // Opcional: ID de actividad (placeholder para validación temporal futura)

    @Column(name = "justificacion", length = 1000)
    private String justificacion; // Opcional: Justificación detallada (obligatoria para AJUSTE, min 20 chars)

    @Column(name = "referencia", nullable = false, length = 500)
    private String referencia;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

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
    protected MovimientoInventarioEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del movimiento
     * @param inventarioItem InventarioItemEntity asociado
     * @param tipo Tipo de movimiento
     * @param cantidad Cantidad del movimiento
     * @param costoUnitario Costo unitario
     * @param costoTotal Costo total
     * @param compraDetalleId ID del detalle de compra (opcional)
     * @param requisicionId ID de la requisición (opcional)
     * @param requisicionItemId ID del ítem de requisición (opcional)
     * @param partidaId ID de la partida (opcional)
     * @param transferenciaId ID de transferencia (opcional)
     * @param actividadId ID de actividad (opcional)
     * @param justificacion Justificación (opcional, obligatoria para AJUSTE)
     * @param referencia Descripción o referencia
     * @param fechaHora Fecha y hora del movimiento
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public MovimientoInventarioEntity(UUID id, InventarioItemEntity inventarioItem,
                                     com.budgetpro.domain.logistica.inventario.model.TipoMovimientoInventario tipo,
                                     BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal costoTotal,
                                     UUID compraDetalleId, UUID requisicionId, UUID requisicionItemId,
                                     UUID partidaId, UUID transferenciaId, UUID actividadId,
                                     String justificacion, String referencia, LocalDateTime fechaHora, Integer version) {
        this.id = id;
        this.inventarioItem = inventarioItem;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.costoTotal = costoTotal;
        this.compraDetalleId = compraDetalleId;
        this.requisicionId = requisicionId;
        this.requisicionItemId = requisicionItemId;
        this.partidaId = partidaId;
        this.transferenciaId = transferenciaId;
        this.actividadId = actividadId;
        this.justificacion = justificacion;
        this.referencia = referencia;
        this.fechaHora = fechaHora;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InventarioItemEntity getInventarioItem() {
        return inventarioItem;
    }

    public void setInventarioItem(InventarioItemEntity inventarioItem) {
        this.inventarioItem = inventarioItem;
    }

    public com.budgetpro.domain.logistica.inventario.model.TipoMovimientoInventario getTipo() {
        return tipo;
    }

    public void setTipo(com.budgetpro.domain.logistica.inventario.model.TipoMovimientoInventario tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public UUID getCompraDetalleId() {
        return compraDetalleId;
    }

    public void setCompraDetalleId(UUID compraDetalleId) {
        this.compraDetalleId = compraDetalleId;
    }

    public UUID getRequisicionId() {
        return requisicionId;
    }

    public void setRequisicionId(UUID requisicionId) {
        this.requisicionId = requisicionId;
    }

    public UUID getRequisicionItemId() {
        return requisicionItemId;
    }

    public void setRequisicionItemId(UUID requisicionItemId) {
        this.requisicionItemId = requisicionItemId;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public UUID getTransferenciaId() {
        return transferenciaId;
    }

    public void setTransferenciaId(UUID transferenciaId) {
        this.transferenciaId = transferenciaId;
    }

    public UUID getActividadId() {
        return actividadId;
    }

    public void setActividadId(UUID actividadId) {
        this.actividadId = actividadId;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
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
