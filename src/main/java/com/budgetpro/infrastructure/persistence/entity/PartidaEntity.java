package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que representa una partida en la base de datos.
 * 
 * Mapea la tabla `partida` del ERD físico definitivo.
 * 
 * Alineado estrictamente con el ERD físico definitivo (_docs/context/08_erd_fisico_definitivo_sql.md):
 * - id UUID
 * - presupuesto_id UUID
 * - codigo VARCHAR(50)
 * - descripcion TEXT
 * - created_at TIMESTAMP
 * - updated_at TIMESTAMP
 */
@Entity
@Table(name = "partida",
       indexes = {
           @Index(name = "idx_partida_presupuesto", columnList = "presupuesto_id")
       })
public class PartidaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_id", nullable = false, updatable = false)
    private PresupuestoEntity presupuesto;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    // WBS Jerárquico (según Directiva Maestra v2.0 y migración V6)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PartidaEntity parent;
    
    @Column(name = "nivel", nullable = false)
    private Integer nivel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requiere constructor sin argumentos
    protected PartidaEntity() {
    }

    /**
     * Constructor público para crear nuevas entidades (partida raíz).
     */
    public PartidaEntity(UUID id, PresupuestoEntity presupuesto, String codigo, String descripcion) {
        this.id = id;
        this.presupuesto = presupuesto;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.parent = null; // Partida raíz
        this.nivel = 1; // Nivel 1 para raíz
    }
    
    /**
     * Constructor público para crear nuevas entidades (partida hija o raíz con WBS).
     * 
     * NOTA: El parent se establecerá después por el mapper si parentId no es null.
     */
    public PartidaEntity(UUID id, PresupuestoEntity presupuesto, String codigo, String descripcion,
                        UUID parentId, int nivel) {
        this.id = id;
        this.presupuesto = presupuesto;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.parent = null; // Se establecerá después por el mapper si parentId no es null
        this.nivel = nivel;
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

    public PresupuestoEntity getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(PresupuestoEntity presupuesto) {
        this.presupuesto = presupuesto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
    
    // WBS: Getters y Setters para jerarquía
    
    public PartidaEntity getParent() {
        return parent;
    }
    
    public void setParent(PartidaEntity parent) {
        this.parent = parent;
    }
    
    /**
     * Obtiene el ID del padre (para consultas directas sin cargar la relación).
     */
    public UUID getParentId() {
        return parent != null ? parent.getId() : null;
    }
    
    /**
     * Establece el parent_id directamente (útil para mapeo desde dominio).
     * 
     * NOTA: Este método establece parent como null. El mapper debe establecer
     * la relación completa usando setParent() con la entidad padre cargada.
     */
    public void setParentId(UUID parentId) {
        // Si parentId es null, establecer parent como null
        if (parentId == null) {
            this.parent = null;
        }
        // Si parentId no es null, el mapper debe establecer la relación completa
        // usando setParent() con la entidad padre cargada desde el repositorio
    }
    
    public Integer getNivel() {
        return nivel;
    }
    
    public void setNivel(int nivel) {
        if (nivel < 1) {
            throw new IllegalArgumentException("El nivel debe ser mayor o igual a 1");
        }
        this.nivel = nivel;
    }
}
