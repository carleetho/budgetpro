package com.budgetpro.infrastructure.persistence.entity.apu;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla apu.
 * 
 * Representa un Análisis de Precios Unitarios asociado a una partida.
 * Relación 1:1 con Partida.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "apu",
       uniqueConstraints = @UniqueConstraint(name = "uq_apu_partida", columnNames = "partida_id"),
       indexes = @Index(name = "idx_apu_partida", columnList = "partida_id"))
public class ApuEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false, updatable = false, unique = true)
    private com.budgetpro.infrastructure.persistence.entity.PartidaEntity partida;

    @Column(name = "rendimiento", precision = 19, scale = 6)
    private BigDecimal rendimiento; // Opcional, cantidad de unidades que se pueden producir por día

    @Column(name = "unidad", length = 20)
    private String unidad; // Copia de la unidad de la partida

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "apu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ApuInsumoEntity> insumos = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected ApuEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del APU
     * @param partida PartidaEntity asociada
     * @param rendimiento Rendimiento (opcional)
     * @param unidad Unidad de medida
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public ApuEntity(UUID id, com.budgetpro.infrastructure.persistence.entity.PartidaEntity partida,
                    BigDecimal rendimiento, String unidad, Integer version) {
        this.id = id;
        this.partida = partida;
        this.rendimiento = rendimiento;
        this.unidad = unidad;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public com.budgetpro.infrastructure.persistence.entity.PartidaEntity getPartida() {
        return partida;
    }

    public void setPartida(com.budgetpro.infrastructure.persistence.entity.PartidaEntity partida) {
        this.partida = partida;
    }

    public BigDecimal getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(BigDecimal rendimiento) {
        this.rendimiento = rendimiento;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
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

    public List<ApuInsumoEntity> getInsumos() {
        return insumos;
    }

    public void setInsumos(List<ApuInsumoEntity> insumos) {
        this.insumos = insumos != null ? insumos : new ArrayList<>();
    }
}
