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
 * Entidad JPA para la tabla kardex.
 */
@Entity
@Table(name = "kardex",
       indexes = {
           @Index(name = "idx_kardex_almacen_recurso", columnList = "almacen_id, recurso_id"),
           @Index(name = "idx_kardex_fecha", columnList = "fecha_movimiento"),
           @Index(name = "idx_kardex_movimiento", columnList = "movimiento_id"),
           @Index(name = "idx_kardex_recurso", columnList = "recurso_id")
       })
public class KardexEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "almacen_id", nullable = false, updatable = false)
    private UUID almacenId;

    @Column(name = "recurso_id", nullable = false, updatable = false)
    private UUID recursoId;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDate fechaMovimiento;

    @Column(name = "movimiento_id", nullable = false, updatable = false)
    private UUID movimientoId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, columnDefinition = "tipo_movimiento_almacen")
    private com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento;

    @Column(name = "cantidad_entrada", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidadEntrada;

    @Column(name = "cantidad_salida", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidadSalida;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "saldo_cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal saldoCantidad;

    @Column(name = "saldo_valor", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoValor;

    @Column(name = "costo_promedio_ponderado", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoPromedioPonderado;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected KardexEntity() {
    }

    public KardexEntity(UUID id, UUID almacenId, UUID recursoId, LocalDate fechaMovimiento,
                       UUID movimientoId, com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento,
                       BigDecimal cantidadEntrada, BigDecimal cantidadSalida, BigDecimal precioUnitario,
                       BigDecimal saldoCantidad, BigDecimal saldoValor, BigDecimal costoPromedioPonderado,
                       Integer version) {
        this.id = id;
        this.almacenId = almacenId;
        this.recursoId = recursoId;
        this.fechaMovimiento = fechaMovimiento;
        this.movimientoId = movimientoId;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidadEntrada = cantidadEntrada;
        this.cantidadSalida = cantidadSalida;
        this.precioUnitario = precioUnitario;
        this.saldoCantidad = saldoCantidad;
        this.saldoValor = saldoValor;
        this.costoPromedioPonderado = costoPromedioPonderado;
        this.version = version;
    }

    // Getters y Setters (simplificados)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAlmacenId() { return almacenId; }
    public void setAlmacenId(UUID almacenId) { this.almacenId = almacenId; }
    public UUID getRecursoId() { return recursoId; }
    public void setRecursoId(UUID recursoId) { this.recursoId = recursoId; }
    public LocalDate getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDate fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
    public UUID getMovimientoId() { return movimientoId; }
    public void setMovimientoId(UUID movimientoId) { this.movimientoId = movimientoId; }
    public com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public BigDecimal getCantidadEntrada() { return cantidadEntrada; }
    public void setCantidadEntrada(BigDecimal cantidadEntrada) { this.cantidadEntrada = cantidadEntrada; }
    public BigDecimal getCantidadSalida() { return cantidadSalida; }
    public void setCantidadSalida(BigDecimal cantidadSalida) { this.cantidadSalida = cantidadSalida; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getSaldoCantidad() { return saldoCantidad; }
    public void setSaldoCantidad(BigDecimal saldoCantidad) { this.saldoCantidad = saldoCantidad; }
    public BigDecimal getSaldoValor() { return saldoValor; }
    public void setSaldoValor(BigDecimal saldoValor) { this.saldoValor = saldoValor; }
    public BigDecimal getCostoPromedioPonderado() { return costoPromedioPonderado; }
    public void setCostoPromedioPonderado(BigDecimal costoPromedioPonderado) { this.costoPromedioPonderado = costoPromedioPonderado; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
