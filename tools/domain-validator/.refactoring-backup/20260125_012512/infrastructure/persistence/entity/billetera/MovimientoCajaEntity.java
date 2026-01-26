package com.budgetpro.infrastructure.persistence.entity.billetera;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla movimiento_caja.
 * 
 * Relación N:1 con Billetera (una billetera tiene muchos movimientos).
 */
@Entity
@Table(name = "movimiento_caja",
       indexes = {
           @Index(name = "idx_movimiento_caja_billetera", columnList = "billetera_id"),
           @Index(name = "idx_movimiento_caja_fecha", columnList = "fecha"),
           @Index(name = "idx_movimiento_caja_tipo", columnList = "tipo")
       })
public class MovimientoCajaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billetera_id", nullable = false, updatable = false)
    private BilleteraEntity billetera;

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private com.budgetpro.domain.finanzas.model.TipoMovimiento tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "referencia", nullable = false, length = 500)
    private String referencia;

    @Column(name = "evidencia_url", length = 1000)
    private String evidenciaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 50)
    private com.budgetpro.domain.finanzas.model.EstadoMovimientoCaja estado;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor protegido para JPA.
     */
    protected MovimientoCajaEntity() {
    }

    public MovimientoCajaEntity(UUID id, BilleteraEntity billetera, BigDecimal monto,
                                com.budgetpro.domain.finanzas.model.TipoMovimiento tipo,
                                LocalDateTime fecha, String referencia, String evidenciaUrl,
                                com.budgetpro.domain.finanzas.model.EstadoMovimientoCaja estado) {
        this.id = id;
        this.billetera = billetera;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
        this.referencia = referencia;
        this.evidenciaUrl = evidenciaUrl;
        this.estado = estado;
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BilleteraEntity getBilletera() {
        return billetera;
    }

    public void setBilletera(BilleteraEntity billetera) {
        this.billetera = billetera;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public com.budgetpro.domain.finanzas.model.TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(com.budgetpro.domain.finanzas.model.TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public void setEvidenciaUrl(String evidenciaUrl) {
        this.evidenciaUrl = evidenciaUrl;
    }

    public com.budgetpro.domain.finanzas.model.EstadoMovimientoCaja getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.finanzas.model.EstadoMovimientoCaja estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
