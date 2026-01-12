package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un movimiento de caja en la base de datos.
 * 
 * Mapea la tabla `movimiento_caja` del ERD físico definitivo.
 * 
 * Alineado con el ERD físico definitivo (_docs/context/08_erd_fisico_definitivo_sql.md):
 * - id UUID
 * - billetera_id UUID (FK a billetera)
 * - tipo VARCHAR(20) (INGRESO | EGRESO)
 * - monto NUMERIC(19,4)
 * - referencia TEXT
 * - evidencia_url TEXT
 * - created_at TIMESTAMP
 * - created_by UUID (auditoría)
 * - trace_id UUID (auditoría)
 */
@Entity
@Table(name = "movimiento_caja",
       indexes = {
           @Index(name = "idx_movimiento_billetera", columnList = "billetera_id"),
           @Index(name = "idx_movimiento_created_at", columnList = "created_at")
       })
public class MovimientoCajaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billetera_id", nullable = false, updatable = false)
    private BilleteraEntity billetera;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // INGRESO o EGRESO

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(name = "referencia", nullable = false, columnDefinition = "TEXT")
    private String referencia;

    @Column(name = "evidencia_url", columnDefinition = "TEXT")
    private String evidenciaUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy; // Auditoría: ID del usuario que creó el movimiento

    @Column(name = "trace_id")
    private UUID traceId; // Auditoría: ID de trazabilidad para correlación

    // JPA requiere constructor sin argumentos
    protected MovimientoCajaEntity() {
    }

    /**
     * Constructor público para crear nuevas entidades.
     */
    public MovimientoCajaEntity(UUID id, BilleteraEntity billetera, String tipo, BigDecimal monto,
                                String referencia, String evidenciaUrl) {
        this.id = id;
        this.billetera = billetera;
        this.tipo = tipo;
        this.monto = normalizarMonto(monto);
        this.referencia = referencia;
        this.evidenciaUrl = evidenciaUrl;
    }

    /**
     * Normaliza un BigDecimal a escala 4 con redondeo HALF_EVEN (Banker's Rounding).
     */
    private static BigDecimal normalizarMonto(BigDecimal monto) {
        if (monto == null) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        }
        return monto.setScale(4, RoundingMode.HALF_EVEN);
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Normalizar monto al persistir
        monto = normalizarMonto(monto);
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

    public UUID getBilleteraId() {
        return billetera != null ? billetera.getId() : null;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = normalizarMonto(monto);
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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }
}
