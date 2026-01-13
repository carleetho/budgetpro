package com.budgetpro.infrastructure.persistence.entity.almacen;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla movimiento_almacen.
 */
@Entity
@Table(name = "movimiento_almacen",
       indexes = {
           @Index(name = "idx_movimiento_almacen", columnList = "almacen_id"),
           @Index(name = "idx_movimiento_recurso", columnList = "recurso_id"),
           @Index(name = "idx_movimiento_tipo", columnList = "tipo_movimiento"),
           @Index(name = "idx_movimiento_fecha", columnList = "fecha_movimiento"),
           @Index(name = "idx_movimiento_partida", columnList = "partida_id")
       })
public class MovimientoAlmacenEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "almacen_id", nullable = false, updatable = false)
    private UUID almacenId;

    @Column(name = "recurso_id", nullable = false, updatable = false)
    private UUID recursoId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, columnDefinition = "tipo_movimiento_almacen")
    private com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "importe_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal importeTotal;

    @Column(name = "numero_documento", length = 100)
    private String numeroDocumento;

    @Column(name = "partida_id")
    private UUID partidaId;

    @Column(name = "centro_costo_id")
    private UUID centroCostoId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
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

    protected MovimientoAlmacenEntity() {
    }

    public MovimientoAlmacenEntity(UUID id, UUID almacenId, UUID recursoId,
                                   com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento,
                                   LocalDate fechaMovimiento, BigDecimal cantidad, BigDecimal precioUnitario,
                                   BigDecimal importeTotal, String numeroDocumento, UUID partidaId,
                                   UUID centroCostoId, String observaciones, Integer version) {
        this.id = id;
        this.almacenId = almacenId;
        this.recursoId = recursoId;
        this.tipoMovimiento = tipoMovimiento;
        this.fechaMovimiento = fechaMovimiento;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.importeTotal = importeTotal;
        this.numeroDocumento = numeroDocumento;
        this.partidaId = partidaId;
        this.centroCostoId = centroCostoId;
        this.observaciones = observaciones;
        this.version = version;
    }

    // Getters y Setters (simplificados)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAlmacenId() { return almacenId; }
    public void setAlmacenId(UUID almacenId) { this.almacenId = almacenId; }
    public UUID getRecursoId() { return recursoId; }
    public void setRecursoId(UUID recursoId) { this.recursoId = recursoId; }
    public com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public LocalDate getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDate fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getImporteTotal() { return importeTotal; }
    public void setImporteTotal(BigDecimal importeTotal) { this.importeTotal = importeTotal; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public UUID getPartidaId() { return partidaId; }
    public void setPartidaId(UUID partidaId) { this.partidaId = partidaId; }
    public UUID getCentroCostoId() { return centroCostoId; }
    public void setCentroCostoId(UUID centroCostoId) { this.centroCostoId = centroCostoId; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
