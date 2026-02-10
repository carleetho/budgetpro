package com.budgetpro.infrastructure.persistence.entity.estimacion;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla estimacion.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "estimacion",
       indexes = {
           @Index(name = "idx_estimacion_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_estimacion_numero", columnList = "proyecto_id, numero_estimacion", unique = true)
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_estimacion_numero", columnNames = {"proyecto_id", "numero_estimacion"})
       })
public class EstimacionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "numero_estimacion", nullable = false)
    // REGLA-130
    private Integer numeroEstimacion;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "monto_bruto", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoBruto;

    @Column(name = "amortizacion_anticipo", nullable = false, precision = 19, scale = 4)
    private BigDecimal amortizacionAnticipo;

    @Column(name = "retencion_fondo_garantia", nullable = false, precision = 19, scale = 4)
    private BigDecimal retencionFondoGarantia;

    @Column(name = "monto_neto_pagar", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoNetoPagar;

    @Column(name = "evidencia_url", length = 1000)
    private String evidenciaUrl;

    // REGLA-066
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion estado;

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
     // REGLA-131
     * Relación con detalles (tabla detalle_estimacion).
     * Una estimación tiene múltiples detalles.
     */
    @OneToMany(mappedBy = "estimacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleEstimacionEntity> detalles = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected EstimacionEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public EstimacionEntity(UUID id, UUID proyectoId, Integer numeroEstimacion,
                           LocalDate fechaCorte, LocalDate periodoInicio, LocalDate periodoFin,
                           BigDecimal montoBruto, BigDecimal amortizacionAnticipo,
                           BigDecimal retencionFondoGarantia, BigDecimal montoNetoPagar,
                           String evidenciaUrl,
                           com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion estado,
                           Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.numeroEstimacion = numeroEstimacion;
        this.fechaCorte = fechaCorte;
        this.periodoInicio = periodoInicio;
        this.periodoFin = periodoFin;
        this.montoBruto = montoBruto;
        this.amortizacionAnticipo = amortizacionAnticipo;
        this.retencionFondoGarantia = retencionFondoGarantia;
        this.montoNetoPagar = montoNetoPagar;
        this.evidenciaUrl = evidenciaUrl;
        this.estado = estado;
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

    public Integer getNumeroEstimacion() {
        return numeroEstimacion;
    }

    public void setNumeroEstimacion(Integer numeroEstimacion) {
        this.numeroEstimacion = numeroEstimacion;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public void setFechaCorte(LocalDate fechaCorte) {
        this.fechaCorte = fechaCorte;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public void setPeriodoInicio(LocalDate periodoInicio) {
        this.periodoInicio = periodoInicio;
    }

    public LocalDate getPeriodoFin() {
        return periodoFin;
    }

    public void setPeriodoFin(LocalDate periodoFin) {
        this.periodoFin = periodoFin;
    }

    public BigDecimal getMontoBruto() {
        return montoBruto;
    }

    public void setMontoBruto(BigDecimal montoBruto) {
        this.montoBruto = montoBruto;
    }

    public BigDecimal getAmortizacionAnticipo() {
        return amortizacionAnticipo;
    }

    public void setAmortizacionAnticipo(BigDecimal amortizacionAnticipo) {
        this.amortizacionAnticipo = amortizacionAnticipo;
    }

    public BigDecimal getRetencionFondoGarantia() {
        return retencionFondoGarantia;
    }

    public void setRetencionFondoGarantia(BigDecimal retencionFondoGarantia) {
        this.retencionFondoGarantia = retencionFondoGarantia;
    }

    public BigDecimal getMontoNetoPagar() {
        return montoNetoPagar;
    }

    public void setMontoNetoPagar(BigDecimal montoNetoPagar) {
        this.montoNetoPagar = montoNetoPagar;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public void setEvidenciaUrl(String evidenciaUrl) {
        this.evidenciaUrl = evidenciaUrl;
    }

    public com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion estado) {
        this.estado = estado;
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

    public List<DetalleEstimacionEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleEstimacionEntity> detalles) {
        this.detalles = detalles;
    }
}
