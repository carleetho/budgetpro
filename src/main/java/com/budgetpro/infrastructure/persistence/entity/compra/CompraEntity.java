package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa una compra en la base de datos.
 * 
 * Mapea la tabla `compra` del ERD físico definitivo.
 * Incluye Optimistic Locking mediante `@Version`.
 * 
 * Alineado con el ERD físico definitivo y el modelo de dominio:
 * - id UUID
 * - proyecto_id UUID
 * - presupuesto_id UUID (requerido por el dominio)
 * - estado VARCHAR (requerido por el dominio)
 * - total NUMERIC(19,4)
 * - version INT
 * - created_at, updated_at
 */
@Entity
@Table(name = "compra",
       indexes = {
           @Index(name = "idx_compra_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_compra_presupuesto", columnList = "presupuesto_id")
       })
public class CompraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "presupuesto_id", nullable = false, updatable = false)
    private UUID presupuestoId;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraDetalleEntity> detalles = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requiere constructor sin argumentos
    protected CompraEntity() {
    }

    /**
     * Constructor público para crear nuevas entidades.
     */
    public CompraEntity(UUID id, UUID proyectoId, UUID presupuestoId, String estado, BigDecimal total, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.presupuestoId = presupuestoId;
        this.estado = estado;
        this.total = total;
        this.version = version;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(UUID presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<CompraDetalleEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<CompraDetalleEntity> detalles) {
        this.detalles = detalles;
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
