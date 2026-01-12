package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa una billetera en la base de datos.
 * 
 * Mapea la tabla `billetera` del ERD físico.
 * Incluye Optimistic Locking mediante `@Version`.
 */
@Entity
@Table(name = "billetera")
public class BilleteraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, unique = true, updatable = false)
    private UUID proyectoId;

    @Column(name = "saldo_actual", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoActual;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requiere constructor sin argumentos
    protected BilleteraEntity() {
    }

    /**
     * Constructor público para crear nuevas entidades.
     */
    public BilleteraEntity(UUID id, UUID proyectoId, BigDecimal saldoActual, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.saldoActual = normalizarMonto(saldoActual);
        this.version = version;
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
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Normalizar saldo al persistir
        saldoActual = normalizarMonto(saldoActual);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Normalizar saldo al actualizar
        saldoActual = normalizarMonto(saldoActual);
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(UUID proyectoId) {
        this.proyectoId = proyectoId;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = normalizarMonto(saldoActual);
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
