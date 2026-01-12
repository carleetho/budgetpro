package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa un ítem de inventario en la base de datos.
 * 
 * Mapea la tabla `inventario_item` del ERD físico definitivo.
 * 
 * Alineado con el ERD físico definitivo (_docs/context/08_erd_fisico_definitivo_sql.md):
 * - id UUID
 * - proyecto_id UUID
 * - recurso_id UUID
 * - cantidad NUMERIC(19,6) (mapeado a stock en dominio)
 * - costo_promedio NUMERIC(19,4)
 * - version INT
 * - created_at, updated_at
 */
@Entity
@Table(name = "inventario_item",
       indexes = {
           @Index(name = "idx_inventario_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_inventario_recurso", columnList = "recurso_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_inventario_proyecto_recurso", columnNames = {"proyecto_id", "recurso_id"})
       })
public class InventarioItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = false, updatable = false)
    private RecursoEntity recurso;

    @Column(name = "cantidad", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidad; // Mapeado a stock en dominio

    @Column(name = "costo_promedio", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoPromedio;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requiere constructor sin argumentos
    protected InventarioItemEntity() {
    }

    /**
     * Constructor público para crear nuevas entidades.
     */
    public InventarioItemEntity(UUID id, UUID proyectoId, RecursoEntity recurso, 
                               BigDecimal cantidad, BigDecimal costoPromedio, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.recurso = recurso;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
        this.costoPromedio = costoPromedio != null ? costoPromedio : BigDecimal.ZERO;
        this.version = version;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        if (cantidad == null) {
            cantidad = BigDecimal.ZERO;
        }
        if (costoPromedio == null) {
            costoPromedio = BigDecimal.ZERO;
        }
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

    public RecursoEntity getRecurso() {
        return recurso;
    }

    public void setRecurso(RecursoEntity recurso) {
        this.recurso = recurso;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
    }

    public BigDecimal getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(BigDecimal costoPromedio) {
        this.costoPromedio = costoPromedio != null ? costoPromedio : BigDecimal.ZERO;
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
