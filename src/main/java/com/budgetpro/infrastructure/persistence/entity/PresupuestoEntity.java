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
 * Entidad JPA que mapea la tabla `presupuesto` del ERD físico.
 * 
 * Esta es una entidad técnica para soportar la FK de `partida`.
 * No tiene agregado de dominio correspondiente aún (solo la entidad técnica).
 */
@Entity
@Table(name = "presupuesto",
       indexes = {
           @Index(name = "idx_presupuesto_proyecto", columnList = "proyecto_id")
       })
public class PresupuestoEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, columnDefinition = "UUID")
    private UUID proyectoId;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "total_asignado", nullable = false, precision = 19, scale = 4, columnDefinition = "NUMERIC(19,4)")
    private BigDecimal totalAsignado = BigDecimal.ZERO;

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartidaEntity> partidas = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors

    public PresupuestoEntity() {
        // Constructor vacío requerido por JPA
    }

    public PresupuestoEntity(UUID id, UUID proyectoId, String nombre, BigDecimal totalAsignado) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.nombre = nombre;
        this.totalAsignado = totalAsignado != null ? totalAsignado : BigDecimal.ZERO;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getTotalAsignado() {
        return totalAsignado;
    }

    public void setTotalAsignado(BigDecimal totalAsignado) {
        this.totalAsignado = totalAsignado;
    }

    public List<PartidaEntity> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<PartidaEntity> partidas) {
        this.partidas = partidas;
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
