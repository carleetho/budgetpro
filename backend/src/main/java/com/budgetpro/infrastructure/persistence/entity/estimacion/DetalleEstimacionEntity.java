package com.budgetpro.infrastructure.persistence.entity.estimacion;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla detalle_estimacion.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "detalle_estimacion",
       indexes = {
           @Index(name = "idx_detalle_estimacion_estimacion", columnList = "estimacion_id"),
           @Index(name = "idx_detalle_estimacion_partida", columnList = "partida_id")
       })
public class DetalleEstimacionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimacion_id", nullable = false, updatable = false)
    private EstimacionEntity estimacion;

    @Column(name = "partida_id", nullable = false, updatable = false)
    private UUID partidaId;

    @Column(name = "cantidad_avance", nullable = false, precision = 19, scale = 4)
    private BigDecimal cantidadAvance;

    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioUnitario;

    @Column(name = "importe", nullable = false, precision = 19, scale = 4)
    private BigDecimal importe;

    @Column(name = "acumulado_anterior", nullable = false, precision = 19, scale = 4)
    private BigDecimal acumuladoAnterior;

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
    protected DetalleEstimacionEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public DetalleEstimacionEntity(UUID id, EstimacionEntity estimacion, UUID partidaId,
                                   BigDecimal cantidadAvance, BigDecimal precioUnitario,
                                   BigDecimal importe, BigDecimal acumuladoAnterior, Integer version) {
        this.id = id;
        this.estimacion = estimacion;
        this.partidaId = partidaId;
        this.cantidadAvance = cantidadAvance;
        this.precioUnitario = precioUnitario;
        this.importe = importe;
        this.acumuladoAnterior = acumuladoAnterior;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EstimacionEntity getEstimacion() {
        return estimacion;
    }

    public void setEstimacion(EstimacionEntity estimacion) {
        this.estimacion = estimacion;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public BigDecimal getCantidadAvance() {
        return cantidadAvance;
    }

    public void setCantidadAvance(BigDecimal cantidadAvance) {
        this.cantidadAvance = cantidadAvance;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public BigDecimal getAcumuladoAnterior() {
        return acumuladoAnterior;
    }

    public void setAcumuladoAnterior(BigDecimal acumuladoAnterior) {
        this.acumuladoAnterior = acumuladoAnterior;
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
