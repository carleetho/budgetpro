package com.budgetpro.infrastructure.persistence.entity.sobrecosto;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla analisis_sobrecosto.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "analisis_sobrecosto",
       indexes = {
           @Index(name = "idx_analisis_sobrecosto_presupuesto", columnList = "presupuesto_id", unique = true)
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_analisis_sobrecosto_presupuesto", columnNames = "presupuesto_id")
       })
public class AnalisisSobrecostoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "presupuesto_id", nullable = false, updatable = false, unique = true)
    private UUID presupuestoId;

    // Indirectos
    @Column(name = "porcentaje_indirectos_oficina_central", nullable = false, precision = 19, scale = 4)
    private BigDecimal porcentajeIndirectosOficinaCentral;

    @Column(name = "porcentaje_indirectos_oficina_campo", nullable = false, precision = 19, scale = 4)
    private BigDecimal porcentajeIndirectosOficinaCampo;

    // Financiamiento
    @Column(name = "porcentaje_financiamiento", nullable = false, precision = 19, scale = 4)
    private BigDecimal porcentajeFinanciamiento;

    @Column(name = "financiamiento_calculado", nullable = false)
    private Boolean financiamientoCalculado;

    // Utilidad
    @Column(name = "porcentaje_utilidad", nullable = false, precision = 19, scale = 4)
    private BigDecimal porcentajeUtilidad;

    // Cargos Adicionales
    @Column(name = "porcentaje_fianzas", nullable = false, precision = 19, scale = 4)
    private BigDecimal porcentajeFianzas;

    @Column(name = "porcentaje_impuestos_reflejables", nullable = false, precision = 19, scale = 4)
    private BigDecimal porcentajeImpuestosReflejables;

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
    protected AnalisisSobrecostoEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public AnalisisSobrecostoEntity(UUID id, UUID presupuestoId,
                                    BigDecimal porcentajeIndirectosOficinaCentral,
                                    BigDecimal porcentajeIndirectosOficinaCampo,
                                    BigDecimal porcentajeFinanciamiento,
                                    Boolean financiamientoCalculado,
                                    BigDecimal porcentajeUtilidad,
                                    BigDecimal porcentajeFianzas,
                                    BigDecimal porcentajeImpuestosReflejables,
                                    Integer version) {
        this.id = id;
        this.presupuestoId = presupuestoId;
        this.porcentajeIndirectosOficinaCentral = porcentajeIndirectosOficinaCentral;
        this.porcentajeIndirectosOficinaCampo = porcentajeIndirectosOficinaCampo;
        this.porcentajeFinanciamiento = porcentajeFinanciamiento;
        this.financiamientoCalculado = financiamientoCalculado;
        this.porcentajeUtilidad = porcentajeUtilidad;
        this.porcentajeFianzas = porcentajeFianzas;
        this.porcentajeImpuestosReflejables = porcentajeImpuestosReflejables;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(UUID presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public BigDecimal getPorcentajeIndirectosOficinaCentral() {
        return porcentajeIndirectosOficinaCentral;
    }

    public void setPorcentajeIndirectosOficinaCentral(BigDecimal porcentajeIndirectosOficinaCentral) {
        this.porcentajeIndirectosOficinaCentral = porcentajeIndirectosOficinaCentral;
    }

    public BigDecimal getPorcentajeIndirectosOficinaCampo() {
        return porcentajeIndirectosOficinaCampo;
    }

    public void setPorcentajeIndirectosOficinaCampo(BigDecimal porcentajeIndirectosOficinaCampo) {
        this.porcentajeIndirectosOficinaCampo = porcentajeIndirectosOficinaCampo;
    }

    public BigDecimal getPorcentajeFinanciamiento() {
        return porcentajeFinanciamiento;
    }

    public void setPorcentajeFinanciamiento(BigDecimal porcentajeFinanciamiento) {
        this.porcentajeFinanciamiento = porcentajeFinanciamiento;
    }

    public Boolean getFinanciamientoCalculado() {
        return financiamientoCalculado;
    }

    public void setFinanciamientoCalculado(Boolean financiamientoCalculado) {
        this.financiamientoCalculado = financiamientoCalculado;
    }

    public BigDecimal getPorcentajeUtilidad() {
        return porcentajeUtilidad;
    }

    public void setPorcentajeUtilidad(BigDecimal porcentajeUtilidad) {
        this.porcentajeUtilidad = porcentajeUtilidad;
    }

    public BigDecimal getPorcentajeFianzas() {
        return porcentajeFianzas;
    }

    public void setPorcentajeFianzas(BigDecimal porcentajeFianzas) {
        this.porcentajeFianzas = porcentajeFianzas;
    }

    public BigDecimal getPorcentajeImpuestosReflejables() {
        return porcentajeImpuestosReflejables;
    }

    public void setPorcentajeImpuestosReflejables(BigDecimal porcentajeImpuestosReflejables) {
        this.porcentajeImpuestosReflejables = porcentajeImpuestosReflejables;
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
