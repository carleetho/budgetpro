package com.budgetpro.infrastructure.persistence.entity.avance;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla valuacion.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "valuacion",
       indexes = {
           @Index(name = "idx_valuacion_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_valuacion_fecha", columnList = "fecha_corte")
       })
public class ValuacionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "fecha_corte", nullable = false, updatable = false)
    private LocalDate fechaCorte;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private com.budgetpro.domain.finanzas.avance.model.EstadoValuacion estado;

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
    protected ValuacionEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public ValuacionEntity(UUID id, UUID proyectoId, LocalDate fechaCorte,
                           String codigo, com.budgetpro.domain.finanzas.avance.model.EstadoValuacion estado,
                           Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.fechaCorte = fechaCorte;
        this.codigo = codigo;
        this.estado = estado;
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

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public void setFechaCorte(LocalDate fechaCorte) {
        this.fechaCorte = fechaCorte;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public com.budgetpro.domain.finanzas.avance.model.EstadoValuacion getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.finanzas.avance.model.EstadoValuacion estado) {
        this.estado = estado;
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
