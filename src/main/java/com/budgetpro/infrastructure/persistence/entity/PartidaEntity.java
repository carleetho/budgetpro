package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla partida.
 * 
 * Representa una partida presupuestaria con estructura jerárquica (WBS).
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "partida",
       indexes = {
           @Index(name = "idx_partida_presupuesto", columnList = "presupuesto_id"),
           @Index(name = "idx_partida_padre", columnList = "padre_id"),
           @Index(name = "idx_partida_item", columnList = "presupuesto_id, item")
       })
public class PartidaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_id", nullable = false, updatable = false)
    private PresupuestoEntity presupuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private PartidaEntity padre; // Relación recursiva para jerarquía

    @Column(name = "item", nullable = false, length = 50)
    private String item; // Código WBS: "01.01", "02.01.05"

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "unidad", length = 20)
    private String unidad; // Opcional si es título

    @Column(name = "metrado", nullable = false, precision = 19, scale = 6)
    private BigDecimal metrado; // Cantidad presupuestada. 0 si es título

    @Column(name = "nivel", nullable = false)
    private Integer nivel; // Profundidad en el árbol: 1, 2, 3...

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
    protected PartidaEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID de la partida
     * @param presupuesto PresupuestoEntity asociado
     * @param padre PartidaEntity padre (puede ser null para partida raíz)
     * @param item Código WBS
     * @param descripcion Descripción de la partida
     * @param unidad Unidad de medida
     * @param metrado Cantidad presupuestada
     * @param nivel Nivel en la jerarquía
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public PartidaEntity(UUID id, PresupuestoEntity presupuesto, PartidaEntity padre,
                        String item, String descripcion, String unidad,
                        BigDecimal metrado, Integer nivel, Integer version) {
        this.id = id;
        this.presupuesto = presupuesto;
        this.padre = padre;
        this.item = item;
        this.descripcion = descripcion;
        this.unidad = unidad;
        this.metrado = metrado;
        this.nivel = nivel;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
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

    public PartidaEntity getPadre() {
        return padre;
    }

    public void setPadre(PartidaEntity padre) {
        this.padre = padre;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getMetrado() {
        return metrado;
    }

    public void setMetrado(BigDecimal metrado) {
        this.metrado = metrado;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
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
