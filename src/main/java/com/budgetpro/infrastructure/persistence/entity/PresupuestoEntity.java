package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA que representa un presupuesto en la base de datos.
 * 
 * Mapea la tabla `presupuesto` del ERD físico con todos los campos del dominio.
 * Incluye Optimistic Locking mediante `@Version`.
 * 
 * Alineado estrictamente con el ERD físico definitivo:
 * - id UUID
 * - proyecto_id UUID
 * - version INT
 * - es_contractual BOOLEAN
 * - created_at, updated_at
 */
@Entity
@Table(name = "presupuesto",
       indexes = {
           @Index(name = "idx_presupuesto_proyecto", columnList = "proyecto_id")
       })
public class PresupuestoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "es_contractual", nullable = false)
    private Boolean esContractual;

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartidaEntity> partidas = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA requiere constructor sin argumentos
    protected PresupuestoEntity() {
    }

    /**
     * Constructor público para crear nuevas entidades.
     */
    public PresupuestoEntity(UUID id, UUID proyectoId, Boolean esContractual, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.esContractual = esContractual != null ? esContractual : false;
        this.version = version;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (esContractual == null) {
            esContractual = false;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getEsContractual() {
        return esContractual;
    }

    public void setEsContractual(Boolean esContractual) {
        this.esContractual = esContractual != null ? esContractual : false;
    }

    public List<PartidaEntity> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<PartidaEntity> partidas) {
        this.partidas = partidas != null ? partidas : new ArrayList<>();
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
