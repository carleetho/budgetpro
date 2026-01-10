package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla `billetera` del ERD físico.
 * Representa la persistencia del agregado Billetera del dominio.
 * 
 * Usa Optimistic Locking mediante @Version para control de concurrencia.
 */
@Entity
@Table(name = "billetera",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_billetera_proyecto", columnNames = "proyecto_id")
       },
       indexes = {
           @Index(name = "idx_billetera_proyecto", columnList = "proyecto_id")
       })
public class BilleteraEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, unique = true, columnDefinition = "UUID")
    private UUID proyectoId;

    @Column(name = "saldo_actual", nullable = false, columnDefinition = "NUMERIC(19,4)")
    private BigDecimal saldoActual;

    /**
     * Campo version para Optimistic Locking.
     * Hibernate incrementa automáticamente este valor en cada UPDATE.
     * Si el valor en BD no coincide con el cargado, lanza OptimisticLockException.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Relación bidireccional One-to-Many con MovimientoEntity.
     * Usa mappedBy para indicar que la relación es manejada por el lado "many".
     * CascadeType.ALL permite que las operaciones se propaguen a los movimientos hijos.
     * orphanRemoval = true elimina movimientos huérfanos al ser removidos de la colección.
     */
    @OneToMany(mappedBy = "billetera", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientoEntity> movimientos = new ArrayList<>();

    // Constructors

    public BilleteraEntity() {
        // Constructor vacío requerido por JPA
    }

    public BilleteraEntity(UUID id, UUID proyectoId, BigDecimal saldoActual, Long version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.saldoActual = saldoActual;
        this.version = version;
    }

    // Getters and Setters

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
        this.saldoActual = saldoActual;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
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

    public List<MovimientoEntity> getMovimientos() {
        return movimientos != null ? new ArrayList<>(movimientos) : new ArrayList<>();
    }

    public void setMovimientos(List<MovimientoEntity> movimientos) {
        this.movimientos = movimientos != null ? new ArrayList<>(movimientos) : new ArrayList<>();
    }

    /**
     * Método helper para agregar un movimiento a la colección bidireccional.
     * Mantiene la sincronización entre ambos lados de la relación.
     */
    public void agregarMovimiento(MovimientoEntity movimiento) {
        if (movimiento == null) {
            return;
        }
        if (this.movimientos == null) {
            this.movimientos = new ArrayList<>();
        }
        this.movimientos.add(movimiento);
        movimiento.setBilletera(this);
    }

    @Override
    public String toString() {
        return String.format("BilleteraEntity{id=%s, proyectoId=%s, saldoActual=%s, version=%d}", 
                           id, proyectoId, saldoActual, version);
    }
}
