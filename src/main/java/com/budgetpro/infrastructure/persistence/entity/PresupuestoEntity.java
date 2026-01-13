package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla presupuesto.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "presupuesto",
       indexes = {
           @Index(name = "idx_presupuesto_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_presupuesto_estado", columnList = "estado")
       })
public class PresupuestoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto estado;

    @Column(name = "es_contractual", nullable = false)
    private Boolean esContractual;

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
    protected PresupuestoEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del presupuesto
     * @param proyectoId ID del proyecto asociado
     * @param nombre Nombre del presupuesto
     * @param estado Estado del presupuesto
     * @param esContractual Si es contractual
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public PresupuestoEntity(UUID id, UUID proyectoId, String nombre,
                            com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto estado,
                            Boolean esContractual, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.nombre = nombre;
        this.estado = estado;
        this.esContractual = esContractual;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto estado) {
        this.estado = estado;
    }

    public Boolean getEsContractual() {
        return esContractual;
    }

    public void setEsContractual(Boolean esContractual) {
        this.esContractual = esContractual;
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
