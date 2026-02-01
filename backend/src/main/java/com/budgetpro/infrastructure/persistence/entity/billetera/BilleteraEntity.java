package com.budgetpro.infrastructure.persistence.entity.billetera;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla billetera.
 * 
 * Relación 1:1 con Proyecto (cada proyecto tiene una billetera).
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * 
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "billetera", uniqueConstraints = @UniqueConstraint(name = "uq_billetera_proyecto", columnNames = "proyecto_id"), indexes = @Index(name = "idx_billetera_proyecto", columnList = "proyecto_id"))
public class BilleteraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false, unique = true)
    private UUID proyectoId;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "saldo_actual", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoActual;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "billetera", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovimientoCajaEntity> movimientos = new ArrayList<>();

    /**
     * Constructor protegido para JPA. CRÍTICO: Acepta version = null. Hibernate
     * inicializará la versión automáticamente.
     */
    protected BilleteraEntity() {
    }

    public BilleteraEntity(UUID id, UUID proyectoId, String moneda, BigDecimal saldoActual, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.moneda = moneda;
        this.saldoActual = saldoActual != null ? saldoActual : BigDecimal.ZERO;
        this.version = version; // CRÍTICO: null para nuevas entidades
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

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
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

    public List<MovimientoCajaEntity> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<MovimientoCajaEntity> movimientos) {
        this.movimientos = movimientos;
    }
}
