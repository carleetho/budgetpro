package com.budgetpro.infrastructure.persistence.entity.reajuste;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla detalle_reajuste_partida.
 */
@Entity
@Table(name = "detalle_reajuste_partida",
       indexes = {
           @Index(name = "idx_detalle_reajuste_estimacion", columnList = "estimacion_reajuste_id"),
           @Index(name = "idx_detalle_reajuste_partida", columnList = "partida_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_detalle_reajuste_partida", columnNames = {"estimacion_reajuste_id", "partida_id"})
       })
public class DetalleReajustePartidaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estimacion_reajuste_id", nullable = false, updatable = false)
    private EstimacionReajusteEntity estimacionReajuste;

    @Column(name = "partida_id", nullable = false, updatable = false)
    private UUID partidaId;

    @Column(name = "monto_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoBase;

    @Column(name = "monto_reajustado", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoReajustado;

    @Column(name = "diferencial", nullable = false, precision = 19, scale = 4)
    private BigDecimal diferencial;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected DetalleReajustePartidaEntity() {
    }

    public DetalleReajustePartidaEntity(UUID id, EstimacionReajusteEntity estimacionReajuste, UUID partidaId,
                                       BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial,
                                       Integer version) {
        this.id = id;
        this.estimacionReajuste = estimacionReajuste;
        this.partidaId = partidaId;
        this.montoBase = montoBase;
        this.montoReajustado = montoReajustado;
        this.diferencial = diferencial;
        this.version = version;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public EstimacionReajusteEntity getEstimacionReajuste() { return estimacionReajuste; }
    public void setEstimacionReajuste(EstimacionReajusteEntity estimacionReajuste) { this.estimacionReajuste = estimacionReajuste; }
    public UUID getPartidaId() { return partidaId; }
    public void setPartidaId(UUID partidaId) { this.partidaId = partidaId; }
    public BigDecimal getMontoBase() { return montoBase; }
    public void setMontoBase(BigDecimal montoBase) { this.montoBase = montoBase; }
    public BigDecimal getMontoReajustado() { return montoReajustado; }
    public void setMontoReajustado(BigDecimal montoReajustado) { this.montoReajustado = montoReajustado; }
    public BigDecimal getDiferencial() { return diferencial; }
    public void setDiferencial(BigDecimal diferencial) { this.diferencial = diferencial; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
