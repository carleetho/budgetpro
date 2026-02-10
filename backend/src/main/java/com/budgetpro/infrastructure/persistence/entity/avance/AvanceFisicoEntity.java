package com.budgetpro.infrastructure.persistence.entity.avance;

import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla avance_fisico.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "avance_fisico",
       indexes = {
           @Index(name = "idx_avance_partida", columnList = "partida_id"),
           @Index(name = "idx_avance_fecha", columnList = "fecha")
       })
// REGLA-068
public class AvanceFisicoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false, updatable = false)
    private PartidaEntity partida;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDate fecha;

    @Column(name = "metrado_ejecutado", nullable = false, precision = 19, scale = 6)
    private BigDecimal metradoEjecutado;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

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
    protected AvanceFisicoEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public AvanceFisicoEntity(UUID id, PartidaEntity partida, LocalDate fecha,
                              BigDecimal metradoEjecutado, String observacion, Integer version) {
        this.id = id;
        this.partida = partida;
        this.fecha = fecha;
        this.metradoEjecutado = metradoEjecutado;
        this.observacion = observacion;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PartidaEntity getPartida() {
        return partida;
    }

    public void setPartida(PartidaEntity partida) {
        this.partida = partida;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMetradoEjecutado() {
        return metradoEjecutado;
    }

    public void setMetradoEjecutado(BigDecimal metradoEjecutado) {
        this.metradoEjecutado = metradoEjecutado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
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
