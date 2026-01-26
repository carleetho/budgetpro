package com.budgetpro.infrastructure.persistence.entity.consumo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla consumo_partida.
 * 
 * Representa el impacto económico real en una partida presupuestaria.
 * 
 * Relación: N:1 con Partida, 1:1 con CompraDetalle
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "consumo_partida",
       indexes = {
           @Index(name = "idx_consumo_partida_partida", columnList = "partida_id"),
           @Index(name = "idx_consumo_partida_compra_detalle", columnList = "compra_detalle_id"),
           @Index(name = "idx_consumo_partida_fecha", columnList = "fecha"),
           @Index(name = "idx_consumo_partida_tipo", columnList = "tipo")
       })
public class ConsumoPartidaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false, updatable = false)
    private com.budgetpro.infrastructure.persistence.entity.PartidaEntity partida;

    @Column(name = "compra_detalle_id")
    private UUID compraDetalleId; // Opcional: relación 1:1 con CompraDetalle (puede ser null)

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private com.budgetpro.domain.finanzas.consumo.model.TipoConsumo tipo;

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
    protected ConsumoPartidaEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del consumo
     * @param partida PartidaEntity asociada
     * @param compraDetalleId ID del detalle de compra (opcional)
     * @param monto Monto del consumo
     * @param fecha Fecha del consumo
     * @param tipo Tipo de consumo
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public ConsumoPartidaEntity(UUID id, com.budgetpro.infrastructure.persistence.entity.PartidaEntity partida,
                                UUID compraDetalleId, BigDecimal monto, LocalDate fecha,
                                com.budgetpro.domain.finanzas.consumo.model.TipoConsumo tipo, Integer version) {
        this.id = id;
        this.partida = partida;
        this.compraDetalleId = compraDetalleId;
        this.monto = monto;
        this.fecha = fecha;
        this.tipo = tipo;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public com.budgetpro.infrastructure.persistence.entity.PartidaEntity getPartida() {
        return partida;
    }

    public void setPartida(com.budgetpro.infrastructure.persistence.entity.PartidaEntity partida) {
        this.partida = partida;
    }

    public UUID getCompraDetalleId() {
        return compraDetalleId;
    }

    public void setCompraDetalleId(UUID compraDetalleId) {
        this.compraDetalleId = compraDetalleId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public com.budgetpro.domain.finanzas.consumo.model.TipoConsumo getTipo() {
        return tipo;
    }

    public void setTipo(com.budgetpro.domain.finanzas.consumo.model.TipoConsumo tipo) {
        this.tipo = tipo;
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
