package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla recepcion.
 * 
 * Representa un evento de recepción de una orden de compra con cumplimiento legal
 * (guía de remisión) y trazabilidad de auditoría (REGLA-167).
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "recepcion",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_recepcion_compra_guia", columnNames = {"compra_id", "guia_remision"})
       },
       indexes = {
           @Index(name = "idx_recepcion_compra", columnList = "compra_id"),
           @Index(name = "idx_recepcion_fecha", columnList = "fecha_recepcion"),
           @Index(name = "idx_recepcion_guia", columnList = "guia_remision")
       })
public class RecepcionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "compra_id", nullable = false, updatable = false)
    private UUID compraId;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDate fechaRecepcion;

    @Column(name = "guia_remision", nullable = false, length = 100)
    private String guiaRemision;

    @Column(name = "creado_por_usuario_id", nullable = false, updatable = false)
    private UUID creadoPorUsuarioId;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecepcionDetalleEntity> detalles = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected RecepcionEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID de la recepción
     * @param compraId ID de la compra asociada
     * @param fechaRecepcion Fecha de recepción
     * @param guiaRemision Número de guía de remisión
     * @param creadoPorUsuarioId ID del usuario que crea la recepción
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public RecepcionEntity(UUID id, UUID compraId, LocalDate fechaRecepcion, String guiaRemision,
                           UUID creadoPorUsuarioId, Long version) {
        this.id = id;
        this.compraId = compraId;
        this.fechaRecepcion = fechaRecepcion;
        this.guiaRemision = guiaRemision;
        this.creadoPorUsuarioId = creadoPorUsuarioId;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompraId() {
        return compraId;
    }

    public void setCompraId(UUID compraId) {
        this.compraId = compraId;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDate fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getGuiaRemision() {
        return guiaRemision;
    }

    public void setGuiaRemision(String guiaRemision) {
        this.guiaRemision = guiaRemision;
    }

    public UUID getCreadoPorUsuarioId() {
        return creadoPorUsuarioId;
    }

    public void setCreadoPorUsuarioId(UUID creadoPorUsuarioId) {
        this.creadoPorUsuarioId = creadoPorUsuarioId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<RecepcionDetalleEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<RecepcionDetalleEntity> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }
}
