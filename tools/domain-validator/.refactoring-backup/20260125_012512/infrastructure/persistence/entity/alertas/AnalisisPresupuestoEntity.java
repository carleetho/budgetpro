package com.budgetpro.infrastructure.persistence.entity.alertas;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla analisis_presupuesto.
 */
@Entity
@Table(name = "analisis_presupuesto",
       indexes = {
           @Index(name = "idx_analisis_presupuesto_presupuesto", columnList = "presupuesto_id"),
           @Index(name = "idx_analisis_presupuesto_fecha", columnList = "fecha_analisis")
       })
public class AnalisisPresupuestoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "presupuesto_id", nullable = false, updatable = false)
    private UUID presupuestoId;

    @Column(name = "fecha_analisis", nullable = false)
    private LocalDateTime fechaAnalisis;

    @Column(name = "total_alertas", nullable = false)
    private Integer totalAlertas;

    @Column(name = "alertas_criticas", nullable = false)
    private Integer alertasCriticas;

    @Column(name = "alertas_warning", nullable = false)
    private Integer alertasWarning;

    @Column(name = "alertas_info", nullable = false)
    private Integer alertasInfo;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "analisis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AlertaParametricaEntity> alertas = new ArrayList<>();

    protected AnalisisPresupuestoEntity() {
    }

    public AnalisisPresupuestoEntity(UUID id, UUID presupuestoId, LocalDateTime fechaAnalisis,
                                    Integer totalAlertas, Integer alertasCriticas, Integer alertasWarning,
                                    Integer alertasInfo, Integer version) {
        this.id = id;
        this.presupuestoId = presupuestoId;
        this.fechaAnalisis = fechaAnalisis;
        this.totalAlertas = totalAlertas;
        this.alertasCriticas = alertasCriticas;
        this.alertasWarning = alertasWarning;
        this.alertasInfo = alertasInfo;
        this.version = version;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPresupuestoId() { return presupuestoId; }
    public void setPresupuestoId(UUID presupuestoId) { this.presupuestoId = presupuestoId; }
    public LocalDateTime getFechaAnalisis() { return fechaAnalisis; }
    public void setFechaAnalisis(LocalDateTime fechaAnalisis) { this.fechaAnalisis = fechaAnalisis; }
    public Integer getTotalAlertas() { return totalAlertas; }
    public void setTotalAlertas(Integer totalAlertas) { this.totalAlertas = totalAlertas; }
    public Integer getAlertasCriticas() { return alertasCriticas; }
    public void setAlertasCriticas(Integer alertasCriticas) { this.alertasCriticas = alertasCriticas; }
    public Integer getAlertasWarning() { return alertasWarning; }
    public void setAlertasWarning(Integer alertasWarning) { this.alertasWarning = alertasWarning; }
    public Integer getAlertasInfo() { return alertasInfo; }
    public void setAlertasInfo(Integer alertasInfo) { this.alertasInfo = alertasInfo; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<AlertaParametricaEntity> getAlertas() { return alertas; }
    public void setAlertas(List<AlertaParametricaEntity> alertas) { this.alertas = alertas; }
}
