package com.budgetpro.infrastructure.persistence.entity;

import com.budgetpro.domain.finanzas.billetera.TipoMovimiento;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla `movimiento_caja` del ERD físico.
 * Representa la persistencia de un Movimiento dentro del agregado Billetera.
 */
@Entity
@Table(name = "movimiento_caja",
       indexes = {
           @Index(name = "idx_movimiento_caja_billetera", columnList = "billetera_id"),
           @Index(name = "idx_movimiento_caja_fecha", columnList = "fecha"),
           @Index(name = "idx_movimiento_caja_tipo", columnList = "tipo")
       })
public class MovimientoEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    /**
     * Relación Many-to-One bidireccional con BilleteraEntity.
     * El lado "many" es el propietario de la relación (tiene la FK).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billetera_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimiento_caja_billetera"))
    private BilleteraEntity billetera;

    @Column(name = "monto", nullable = false, columnDefinition = "NUMERIC(19,4)")
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "referencia", nullable = false, length = 255)
    private String referencia;

    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors

    public MovimientoEntity() {
        // Constructor vacío requerido por JPA
    }

    public MovimientoEntity(UUID id, BilleteraEntity billetera, BigDecimal monto, TipoMovimiento tipo,
                           LocalDateTime fecha, String referencia, String evidenciaUrl, String estado) {
        this.id = id;
        this.billetera = billetera;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
        this.referencia = referencia;
        this.evidenciaUrl = evidenciaUrl;
        this.estado = estado != null ? estado : "ACTIVO";
    }

    // Getters and Setters

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

    /**
     * Método helper para obtener el ID de la billetera sin cargar la entidad completa.
     * Útil para consultas y acceso directo al ID.
     */
    public UUID getBilleteraId() {
        return billetera != null ? billetera.getId() : null;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado != null ? estado : "ACTIVO";
    }

    @Override
    public String toString() {
        return String.format("MovimientoEntity{id=%s, billeteraId=%s, tipo=%s, monto=%s, referencia='%s', fecha=%s, estado=%s}", 
                           id, getBilleteraId(), tipo, monto, referencia, fecha, estado != null ? estado : "ACTIVO");
    }
}
