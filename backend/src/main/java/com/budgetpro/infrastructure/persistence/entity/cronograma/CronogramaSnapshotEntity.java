package com.budgetpro.infrastructure.persistence.entity.cronograma;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla cronograma_snapshot.
 * 
 * Representa un snapshot inmutable del cronograma congelado (baseline).
 * Los datos temporales complejos se almacenan como JSONB para flexibilidad.
 */
@Entity
@Table(name = "cronograma_snapshot",
       indexes = {
           @Index(name = "idx_cronograma_snapshot_programa_obra", columnList = "programa_obra_id"),
           @Index(name = "idx_cronograma_snapshot_presupuesto", columnList = "presupuesto_id")
       })
public class CronogramaSnapshotEntity {

    @Id
    @Column(name = "snapshot_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "programa_obra_id", nullable = false, updatable = false)
    private UUID programaObraId;

    @Column(name = "presupuesto_id", nullable = false, updatable = false)
    private UUID presupuestoId;

    // Snapshot data stored as JSONB
    /**
     * Datos de fechas del cronograma en formato JSONB.
     */
    @Column(name = "fechas_snapshot", columnDefinition = "jsonb", nullable = false)
    private String fechasJson;

    /**
     * Datos de duraciones del cronograma en formato JSONB.
     */
    @Column(name = "duraciones_snapshot", columnDefinition = "jsonb", nullable = false)
    private String duracionesJson;

    /**
     * Datos de secuencia y dependencias en formato JSONB.
     */
    @Column(name = "secuencia_snapshot", columnDefinition = "jsonb", nullable = false)
    private String secuenciaJson;

    /**
     * Datos de calendarios y restricciones temporales en formato JSONB.
     */
    @Column(name = "calendarios_snapshot", columnDefinition = "jsonb", nullable = false)
    private String calendariosJson;

    // Snapshot metadata
    /**
     * Fecha y hora en que se creó el snapshot.
     */
    @CreationTimestamp
    @Column(name = "snapshot_date", nullable = false, updatable = false)
    private LocalDateTime snapshotDate;

    /**
     * Versión del algoritmo usado para generar el snapshot.
     */
    @Column(name = "snapshot_algorithm", length = 50, nullable = false, updatable = false)
    private String snapshotAlgorithm;

    /**
     * Constructor protegido para JPA.
     */
    protected CronogramaSnapshotEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public CronogramaSnapshotEntity(UUID id, UUID programaObraId, UUID presupuestoId,
                                   String fechasJson, String duracionesJson, String secuenciaJson,
                                   String calendariosJson, LocalDateTime snapshotDate, String snapshotAlgorithm) {
        this.id = id;
        this.programaObraId = programaObraId;
        this.presupuestoId = presupuestoId;
        this.fechasJson = fechasJson;
        this.duracionesJson = duracionesJson;
        this.secuenciaJson = secuenciaJson;
        this.calendariosJson = calendariosJson;
        this.snapshotDate = snapshotDate;
        this.snapshotAlgorithm = snapshotAlgorithm;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProgramaObraId() {
        return programaObraId;
    }

    public void setProgramaObraId(UUID programaObraId) {
        this.programaObraId = programaObraId;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(UUID presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public String getFechasJson() {
        return fechasJson;
    }

    public void setFechasJson(String fechasJson) {
        this.fechasJson = fechasJson;
    }

    public String getDuracionesJson() {
        return duracionesJson;
    }

    public void setDuracionesJson(String duracionesJson) {
        this.duracionesJson = duracionesJson;
    }

    public String getSecuenciaJson() {
        return secuenciaJson;
    }

    public void setSecuenciaJson(String secuenciaJson) {
        this.secuenciaJson = secuenciaJson;
    }

    public String getCalendariosJson() {
        return calendariosJson;
    }

    public void setCalendariosJson(String calendariosJson) {
        this.calendariosJson = calendariosJson;
    }

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public String getSnapshotAlgorithm() {
        return snapshotAlgorithm;
    }

    public void setSnapshotAlgorithm(String snapshotAlgorithm) {
        this.snapshotAlgorithm = snapshotAlgorithm;
    }
}
