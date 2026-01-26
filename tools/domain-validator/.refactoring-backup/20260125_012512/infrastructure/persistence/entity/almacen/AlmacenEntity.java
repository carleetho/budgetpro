package com.budgetpro.infrastructure.persistence.entity.almacen;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla almacen.
 */
@Entity
@Table(name = "almacen",
       indexes = {
           @Index(name = "idx_almacen_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_almacen_codigo", columnList = "codigo"),
           @Index(name = "idx_almacen_activo", columnList = "activo")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_almacen_codigo_proyecto", columnNames = {"proyecto_id", "codigo"})
       })
public class AlmacenEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "ubicacion", columnDefinition = "TEXT")
    private String ubicacion;

    @Column(name = "responsable_id")
    private UUID responsableId;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AlmacenEntity() {
    }

    public AlmacenEntity(UUID id, UUID proyectoId, String codigo, String nombre,
                        String ubicacion, UUID responsableId, Boolean activo, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.responsableId = responsableId;
        this.activo = activo;
        this.version = version;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProyectoId() { return proyectoId; }
    public void setProyectoId(UUID proyectoId) { this.proyectoId = proyectoId; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public UUID getResponsableId() { return responsableId; }
    public void setResponsableId(UUID responsableId) { this.responsableId = responsableId; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
