package com.budgetpro.infrastructure.persistence.entity.reajuste;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla estimacion_reajuste.
 */
@Entity
@Table(name = "estimacion_reajuste",
       indexes = {
           @Index(name = "idx_estimacion_reajuste_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_estimacion_reajuste_presupuesto", columnList = "presupuesto_id"),
           @Index(name = "idx_estimacion_reajuste_fecha_corte", columnList = "fecha_corte"),
           @Index(name = "idx_estimacion_reajuste_estado", columnList = "estado")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_estimacion_reajuste_numero", columnNames = {"proyecto_id", "numero_estimacion"})
       })
public class EstimacionReajusteEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "presupuesto_id", nullable = false, updatable = false)
    private UUID presupuestoId;

    @Column(name = "numero_estimacion", nullable = false)
    private Integer numeroEstimacion;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(name = "indice_base_codigo", nullable = false, length = 50)
    private String indiceBaseCodigo;

    @Column(name = "indice_base_fecha", nullable = false)
    private LocalDate indiceBaseFecha;

    @Column(name = "indice_actual_codigo", nullable = false, length = 50)
    private String indiceActualCodigo;

    @Column(name = "indice_actual_fecha", nullable = false)
    private LocalDate indiceActualFecha;

    @Column(name = "valor_indice_base", nullable = false, precision = 19, scale = 6)
    private BigDecimal valorIndiceBase;

    @Column(name = "valor_indice_actual", nullable = false, precision = 19, scale = 6)
    private BigDecimal valorIndiceActual;

    @Column(name = "monto_base", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoBase;

    @Column(name = "monto_reajustado", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoReajustado;

    @Column(name = "diferencial", nullable = false, precision = 19, scale = 4)
    private BigDecimal diferencial;

    @Column(name = "porcentaje_variacion", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeVariacion;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private com.budgetpro.domain.finanzas.reajuste.model.EstadoEstimacionReajuste estado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "estimacionReajuste", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleReajustePartidaEntity> detalles = new ArrayList<>();

    protected EstimacionReajusteEntity() {
    }

    public EstimacionReajusteEntity(UUID id, UUID proyectoId, UUID presupuestoId, Integer numeroEstimacion,
                                   LocalDate fechaCorte, String indiceBaseCodigo, LocalDate indiceBaseFecha,
                                   String indiceActualCodigo, LocalDate indiceActualFecha,
                                   BigDecimal valorIndiceBase, BigDecimal valorIndiceActual,
                                   BigDecimal montoBase, BigDecimal montoReajustado, BigDecimal diferencial,
                                   BigDecimal porcentajeVariacion,
                                   com.budgetpro.domain.finanzas.reajuste.model.EstadoEstimacionReajuste estado,
                                   String observaciones, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.presupuestoId = presupuestoId;
        this.numeroEstimacion = numeroEstimacion;
        this.fechaCorte = fechaCorte;
        this.indiceBaseCodigo = indiceBaseCodigo;
        this.indiceBaseFecha = indiceBaseFecha;
        this.indiceActualCodigo = indiceActualCodigo;
        this.indiceActualFecha = indiceActualFecha;
        this.valorIndiceBase = valorIndiceBase;
        this.valorIndiceActual = valorIndiceActual;
        this.montoBase = montoBase;
        this.montoReajustado = montoReajustado;
        this.diferencial = diferencial;
        this.porcentajeVariacion = porcentajeVariacion;
        this.estado = estado;
        this.observaciones = observaciones;
        this.version = version;
    }

    // Getters y Setters (simplificados - solo los esenciales)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProyectoId() { return proyectoId; }
    public void setProyectoId(UUID proyectoId) { this.proyectoId = proyectoId; }
    public UUID getPresupuestoId() { return presupuestoId; }
    public void setPresupuestoId(UUID presupuestoId) { this.presupuestoId = presupuestoId; }
    public Integer getNumeroEstimacion() { return numeroEstimacion; }
    public void setNumeroEstimacion(Integer numeroEstimacion) { this.numeroEstimacion = numeroEstimacion; }
    public LocalDate getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(LocalDate fechaCorte) { this.fechaCorte = fechaCorte; }
    public String getIndiceBaseCodigo() { return indiceBaseCodigo; }
    public void setIndiceBaseCodigo(String indiceBaseCodigo) { this.indiceBaseCodigo = indiceBaseCodigo; }
    public LocalDate getIndiceBaseFecha() { return indiceBaseFecha; }
    public void setIndiceBaseFecha(LocalDate indiceBaseFecha) { this.indiceBaseFecha = indiceBaseFecha; }
    public String getIndiceActualCodigo() { return indiceActualCodigo; }
    public void setIndiceActualCodigo(String indiceActualCodigo) { this.indiceActualCodigo = indiceActualCodigo; }
    public LocalDate getIndiceActualFecha() { return indiceActualFecha; }
    public void setIndiceActualFecha(LocalDate indiceActualFecha) { this.indiceActualFecha = indiceActualFecha; }
    public BigDecimal getValorIndiceBase() { return valorIndiceBase; }
    public void setValorIndiceBase(BigDecimal valorIndiceBase) { this.valorIndiceBase = valorIndiceBase; }
    public BigDecimal getValorIndiceActual() { return valorIndiceActual; }
    public void setValorIndiceActual(BigDecimal valorIndiceActual) { this.valorIndiceActual = valorIndiceActual; }
    public BigDecimal getMontoBase() { return montoBase; }
    public void setMontoBase(BigDecimal montoBase) { this.montoBase = montoBase; }
    public BigDecimal getMontoReajustado() { return montoReajustado; }
    public void setMontoReajustado(BigDecimal montoReajustado) { this.montoReajustado = montoReajustado; }
    public BigDecimal getDiferencial() { return diferencial; }
    public void setDiferencial(BigDecimal diferencial) { this.diferencial = diferencial; }
    public BigDecimal getPorcentajeVariacion() { return porcentajeVariacion; }
    public void setPorcentajeVariacion(BigDecimal porcentajeVariacion) { this.porcentajeVariacion = porcentajeVariacion; }
    public com.budgetpro.domain.finanzas.reajuste.model.EstadoEstimacionReajuste getEstado() { return estado; }
    public void setEstado(com.budgetpro.domain.finanzas.reajuste.model.EstadoEstimacionReajuste estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<DetalleReajustePartidaEntity> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleReajustePartidaEntity> detalles) { this.detalles = detalles; }
}
