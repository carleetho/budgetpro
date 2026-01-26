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

    @Column(name = "retencion_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal retencionPorcentaje;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "aprobado_por")
    private UUID aprobadoPor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "estimacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EstimacionItemEntity> items = new ArrayList<>();

    // Required by JPA
    public EstimacionEntity() {
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

    public List<EstimacionItemEntity> getItems() {
        return items;
    }

    public void setItems(List<EstimacionItemEntity> items) {
        this.items = items;
        for (EstimacionItemEntity item : items) {
            item.setEstimacion(this);
        }
    }

    public void addItem(EstimacionItemEntity item) {
        this.items.add(item);
        item.setEstimacion(this);
    }
}
