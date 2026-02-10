package com.budgetpro.infrastructure.persistence.entity.inventario;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla inventario_item.
 * 
 * Representa el stock físico de un recurso en un proyecto y bodega específica.
 * Un registro por Proyecto + RecursoExternalId + UnidadBase + Bodega (UNIQUE).
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "inventario_item",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_inventario_proyecto_recurso_unidad_bodega", 
                           columnNames = {"proyecto_id", "recurso_external_id", "unidad_base", "bodega_id"})
       },
       indexes = {
           @Index(name = "idx_inventario_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_inventario_recurso", columnList = "recurso_id"),
           @Index(name = "idx_inventario_bodega", columnList = "bodega_id"),
           @Index(name = "idx_inventario_recurso_external", columnList = "recurso_external_id")
       })
// REGLA-136
public class InventarioItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_id", nullable = true, updatable = false)
    @Deprecated
    private com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso; // DEPRECATED: Mantener para migración

    @Column(name = "recurso_external_id", nullable = false, length = 255, updatable = false)
    private String recursoExternalId; // ID externo del recurso (ej. "MAT-001")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_id", nullable = false, updatable = false)
    private com.budgetpro.infrastructure.persistence.entity.BodegaEntity bodega;

    // Campos snapshot (inmutables después de creación)
    @Column(name = "recurso_nombre", nullable = false, length = 500, updatable = false)
    private String nombre; // Nombre del recurso (snapshot)

    @Column(name = "recurso_clasificacion", nullable = false, length = 100, updatable = false)
    private String clasificacion; // Clasificación del recurso (snapshot)

    @Column(name = "unidad_base", nullable = false, length = 20, updatable = false)
    private String unidadBase; // Unidad base del recurso (snapshot)

    @Column(name = "cantidad_fisica", nullable = false, precision = 19, scale = 6)
    // REGLA-137
    private BigDecimal cantidadFisica;

    @Column(name = "costo_promedio", nullable = false, precision = 19, scale = 4)
    private BigDecimal costoPromedio;

    @Deprecated
    @Column(name = "ubicacion", length = 200)
    private String ubicacion; // DEPRECATED: Usar bodega_id en su lugar

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "inventarioItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovimientoInventarioEntity> movimientos = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected InventarioItemEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del item de inventario
     * @param proyectoId ID del proyecto
     * @param recurso RecursoEntity asociado (puede ser null durante migración)
     * @param recursoExternalId ID externo del recurso (ej. "MAT-001")
     * @param bodega BodegaEntity donde se almacena
     * @param nombre Nombre del recurso (snapshot)
     * @param clasificacion Clasificación del recurso (snapshot)
     * @param unidadBase Unidad base del recurso (snapshot)
     * @param cantidadFisica Cantidad física en stock
     * @param costoPromedio Costo promedio ponderado
     * @param ubicacion Ubicación en el almacén (DEPRECATED)
     * @param ultimaActualizacion Fecha de última actualización
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public InventarioItemEntity(UUID id, UUID proyectoId,
                                com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso,
                                String recursoExternalId,
                                com.budgetpro.infrastructure.persistence.entity.BodegaEntity bodega,
                                String nombre, String clasificacion, String unidadBase,
                                BigDecimal cantidadFisica, BigDecimal costoPromedio,
                                String ubicacion, LocalDateTime ultimaActualizacion, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.recurso = recurso;
        this.recursoExternalId = recursoExternalId;
        this.bodega = bodega;
        this.nombre = nombre;
        this.clasificacion = clasificacion;
        this.unidadBase = unidadBase;
        this.cantidadFisica = cantidadFisica;
        this.costoPromedio = costoPromedio;
        this.ubicacion = ubicacion;
        this.ultimaActualizacion = ultimaActualizacion;
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

    @Deprecated
    public com.budgetpro.infrastructure.persistence.entity.RecursoEntity getRecurso() {
        return recurso;
    }

    @Deprecated
    public void setRecurso(com.budgetpro.infrastructure.persistence.entity.RecursoEntity recurso) {
        this.recurso = recurso;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public void setRecursoExternalId(String recursoExternalId) {
        this.recursoExternalId = recursoExternalId;
    }

    public com.budgetpro.infrastructure.persistence.entity.BodegaEntity getBodega() {
        return bodega;
    }

    public void setBodega(com.budgetpro.infrastructure.persistence.entity.BodegaEntity bodega) {
        this.bodega = bodega;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public String getUnidadBase() {
        return unidadBase;
    }

    public void setUnidadBase(String unidadBase) {
        this.unidadBase = unidadBase;
    }

    public BigDecimal getCantidadFisica() {
        return cantidadFisica;
    }

    public void setCantidadFisica(BigDecimal cantidadFisica) {
        this.cantidadFisica = cantidadFisica;
    }

    public BigDecimal getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(BigDecimal costoPromedio) {
        this.costoPromedio = costoPromedio;
    }

    @Deprecated
    public String getUbicacion() {
        return ubicacion;
    }

    @Deprecated
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
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

    public List<MovimientoInventarioEntity> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<MovimientoInventarioEntity> movimientos) {
        this.movimientos = movimientos != null ? movimientos : new ArrayList<>();
    }
}
