package com.budgetpro.infrastructure.persistence.entity.reajuste;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla indice_precios.
 */
@Entity
@Table(name = "indice_precios",
       indexes = {
           @Index(name = "idx_indice_precios_codigo", columnList = "codigo"),
           @Index(name = "idx_indice_precios_fecha", columnList = "fecha_base"),
           @Index(name = "idx_indice_precios_tipo", columnList = "tipo"),
           @Index(name = "idx_indice_precios_activo", columnList = "activo")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_indice_precios_codigo_fecha", columnNames = {"codigo", "fecha_base"})
       })
public class IndicePreciosEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "tipo_indice_precios")
    private com.budgetpro.domain.finanzas.reajuste.model.TipoIndicePrecios tipo;

    @Column(name = "fecha_base", nullable = false)
    private LocalDate fechaBase;

    @Column(name = "valor", nullable = false, precision = 19, scale = 6)
    private BigDecimal valor;

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

    protected IndicePreciosEntity() {
    }

    public IndicePreciosEntity(UUID id, String codigo, String nombre,
                              com.budgetpro.domain.finanzas.reajuste.model.TipoIndicePrecios tipo,
                              LocalDate fechaBase, BigDecimal valor, Boolean activo, Integer version) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.fechaBase = fechaBase;
        this.valor = valor;
        this.activo = activo;
        this.version = version;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public com.budgetpro.domain.finanzas.reajuste.model.TipoIndicePrecios getTipo() { return tipo; }
    public void setTipo(com.budgetpro.domain.finanzas.reajuste.model.TipoIndicePrecios tipo) { this.tipo = tipo; }
    public LocalDate getFechaBase() { return fechaBase; }
    public void setFechaBase(LocalDate fechaBase) { this.fechaBase = fechaBase; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
