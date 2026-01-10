package com.budgetpro.infrastructure.persistence.entity;

import com.budgetpro.domain.recurso.model.EstadoRecurso;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla `recurso` del ERD físico.
 * Representa la persistencia del agregado Recurso del dominio.
 */
@Entity
@Table(name = "recurso", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_recurso_nombre", columnNames = "nombre_normalizado")
       },
       indexes = {
           @Index(name = "idx_recurso_tipo", columnList = "tipo"),
           @Index(name = "idx_recurso_estado", columnList = "estado")
       })
public class RecursoEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "nombre", nullable = false, columnDefinition = "TEXT")
    private String nombre;

    @Column(name = "nombre_normalizado", nullable = false, unique = true, columnDefinition = "TEXT")
    private String nombreNormalizado;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "recurso_tipo")
    private TipoRecurso tipo;

    @Column(name = "unidad_base", nullable = false, length = 20)
    private String unidadBase;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "atributos", nullable = false, columnDefinition = "JSONB")
    private Map<String, Object> atributos = new HashMap<>();

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "recurso_estado")
    private EstadoRecurso estado = EstadoRecurso.ACTIVO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, columnDefinition = "UUID")
    private UUID createdBy;

    // Constructors

    public RecursoEntity() {
        // Constructor vacío requerido por JPA
    }

    public RecursoEntity(UUID id, String nombre, String nombreNormalizado, TipoRecurso tipo, 
                         String unidadBase, Map<String, Object> atributos, EstadoRecurso estado, 
                         UUID createdBy) {
        this.id = id;
        this.nombre = nombre;
        this.nombreNormalizado = nombreNormalizado;
        this.tipo = tipo;
        this.unidadBase = unidadBase;
        this.atributos = atributos != null ? new HashMap<>(atributos) : new HashMap<>();
        this.estado = estado != null ? estado : EstadoRecurso.ACTIVO;
        this.createdBy = createdBy;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreNormalizado() {
        return nombreNormalizado;
    }

    public void setNombreNormalizado(String nombreNormalizado) {
        this.nombreNormalizado = nombreNormalizado;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public void setTipo(TipoRecurso tipo) {
        this.tipo = tipo;
    }

    public String getUnidadBase() {
        return unidadBase;
    }

    public void setUnidadBase(String unidadBase) {
        this.unidadBase = unidadBase;
    }

    public Map<String, Object> getAtributos() {
        return atributos != null ? new HashMap<>(atributos) : new HashMap<>();
    }

    public void setAtributos(Map<String, Object> atributos) {
        this.atributos = atributos != null ? new HashMap<>(atributos) : new HashMap<>();
    }

    public EstadoRecurso getEstado() {
        return estado;
    }

    public void setEstado(EstadoRecurso estado) {
        this.estado = estado;
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
}
