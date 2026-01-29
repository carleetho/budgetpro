package com.budgetpro.infrastructure.persistence.entity.estimacion;

import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "estimacion")
public class EstimacionEntity {

    @Id
    @Column(name = "estimacion_id")
    private UUID id;

    @Column(name = "presupuesto_id", nullable = false)
    private UUID presupuestoId;

    @Column(name = "numero_estimacion", nullable = false)
    private Long numeroEstimacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoEstimacion estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "fecha_corte")
    private LocalDateTime fechaCorte;

    @Column(name = "retencion_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal retencionPorcentaje;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "aprobado_por")
    private UUID aprobadoPor;

    // Financial fields
    @Column(name = "monto_bruto", precision = 19, scale = 4)
    private BigDecimal montoBruto;

    @Column(name = "amortizacion_anticipo", precision = 19, scale = 4)
    private BigDecimal amortizacionAnticipo;

    @Column(name = "retencion_fondo_garantia", precision = 19, scale = 4)
    private BigDecimal retencionFondoGarantia;

    @Column(name = "monto_neto_pagar", precision = 19, scale = 4)
    private BigDecimal montoNetoPagar;

    @Column(name = "evidencia_url", length = 500)
    private String evidenciaUrl;

    @Version
    @Column(name = "version")
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "estimacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleEstimacionEntity> detalles = new ArrayList<>();

    // Required by JPA
    public EstimacionEntity() {
    }

    public EstimacionEntity(UUID id, UUID presupuestoId, Long numeroEstimacion, LocalDateTime fechaCorte,
            LocalDate fechaInicio, LocalDate fechaFin, BigDecimal montoBruto, BigDecimal amortizacionAnticipo,
            BigDecimal retencionFondoGarantia, BigDecimal montoNetoPagar, String evidenciaUrl, EstadoEstimacion estado,
            Integer version) {
        this.id = id;
        this.presupuestoId = presupuestoId;
        this.numeroEstimacion = numeroEstimacion;
        this.fechaCorte = fechaCorte;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.montoBruto = montoBruto;
        this.amortizacionAnticipo = amortizacionAnticipo;
        this.retencionFondoGarantia = retencionFondoGarantia;
        this.montoNetoPagar = montoNetoPagar;
        this.evidenciaUrl = evidenciaUrl;
        this.estado = estado;
        this.version = version;
        this.fechaCreacion = LocalDateTime.now();
        this.retencionPorcentaje = BigDecimal.TEN;
    }

    // Getters and Setters
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

    public Long getNumeroEstimacion() {
        return numeroEstimacion;
    }

    public void setNumeroEstimacion(Long numeroEstimacion) {
        this.numeroEstimacion = numeroEstimacion;
    }

    public EstadoEstimacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoEstimacion estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaCorte() {
        return fechaCorte;
    }

    public void setFechaCorte(LocalDateTime fechaCorte) {
        this.fechaCorte = fechaCorte;
    }

    public BigDecimal getRetencionPorcentaje() {
        return retencionPorcentaje;
    }

    public void setRetencionPorcentaje(BigDecimal retencionPorcentaje) {
        this.retencionPorcentaje = retencionPorcentaje;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public UUID getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(UUID aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
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
        if (detalles != null) {
            detalles.forEach(d -> d.setEstimacion(this));
        }
    }
}
