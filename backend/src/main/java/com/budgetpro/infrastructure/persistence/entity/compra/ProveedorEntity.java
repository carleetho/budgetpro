package com.budgetpro.infrastructure.persistence.entity.compra;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla proveedor.
 * 
 * Representa un proveedor de bienes o servicios para el proyecto.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "proveedor",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_proveedor_ruc", columnNames = "ruc")
       },
       indexes = {
           @Index(name = "idx_proveedor_ruc", columnList = "ruc"),
           @Index(name = "idx_proveedor_estado", columnList = "estado"),
           @Index(name = "idx_proveedor_razon_social", columnList = "razon_social")
       })
public class ProveedorEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "ruc", nullable = false, unique = true, length = 20)
    private String ruc;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private com.budgetpro.domain.logistica.compra.model.ProveedorEstado estado;

    @Column(name = "contacto", length = 200)
    private String contacto;

    @Column(name = "direccion", length = 500)
    private String direccion;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected ProveedorEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del proveedor
     * @param razonSocial Razón social del proveedor
     * @param ruc RUC (número de identificación tributaria)
     * @param estado Estado del proveedor
     * @param contacto Información de contacto (opcional)
     * @param direccion Dirección física (opcional)
     * @param version Versión (puede ser null para nuevas entidades)
     * @param createdBy ID del usuario que crea el proveedor
     * @param updatedBy ID del usuario que actualiza el proveedor
     */
    public ProveedorEntity(UUID id, String razonSocial, String ruc,
                          com.budgetpro.domain.logistica.compra.model.ProveedorEstado estado,
                          String contacto, String direccion, Integer version,
                          UUID createdBy, UUID updatedBy) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.estado = estado;
        this.contacto = contacto;
        this.direccion = direccion;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public com.budgetpro.domain.logistica.compra.model.ProveedorEstado getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.logistica.compra.model.ProveedorEstado estado) {
        this.estado = estado;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }
}
