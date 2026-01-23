package com.budgetpro.domain.finanzas.cronograma.model;

import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad inmutable que representa un snapshot (baseline) del cronograma congelado.
 * 
 * Esta entidad captura y preserva el estado temporal completo del cronograma
 * en el momento del congelamiento, estableciendo un baseline inmutable.
 * 
 * **Relaciones:**
 * - One-to-one con ProgramaObra (un snapshot por cronograma congelado)
 * - Many-to-one con Presupuesto (múltiples snapshots por presupuesto si hay re-baseline)
 * 
 * **Diseño Inmutable:**
 * - Todos los campos son final y privados
 * - No hay métodos de mutación (setters)
 * - Solo getters para acceso a datos
 * - Factory method para creación
 * 
 * **Almacenamiento JSONB:**
 * Los datos temporales complejos (fechas, duraciones, secuencia, calendarios)
 * se almacenan como JSONB para flexibilidad y simplicidad.
 * 
 * **Versión del Algoritmo:**
 * El campo snapshotAlgorithm permite migración futura a diferentes formatos
 * de snapshot sin romper compatibilidad.
 * 
 * Contexto: Cronograma & Baseline
 */
public final class CronogramaSnapshot {

    private final CronogramaSnapshotId id;
    private final ProgramaObraId programaObraId;
    private final PresupuestoId presupuestoId;
    
    // Snapshot data stored as JSONB strings
    /**
     * Datos de fechas del cronograma en formato JSONB.
     * Incluye fechaInicio, fechaFinEstimada, y todas las fechas de actividades.
     */
    private final String fechasJson;
    
    /**
     * Datos de duraciones del cronograma en formato JSONB.
     * Incluye duracionTotalDias y duraciones de actividades.
     */
    private final String duracionesJson;
    
    /**
     * Datos de secuencia y dependencias en formato JSONB.
     * Incluye orden de actividades y relaciones de precedencia.
     */
    private final String secuenciaJson;
    
    /**
     * Datos de calendarios y restricciones temporales en formato JSONB.
     * Incluye calendarios de trabajo, días festivos, restricciones.
     */
    private final String calendariosJson;
    
    // Snapshot metadata
    /**
     * Fecha y hora en que se creó el snapshot.
     */
    private final LocalDateTime snapshotDate;
    
    /**
     * Versión del algoritmo usado para generar el snapshot.
     * Formato: "TEMPORAL-SNAPSHOT-v1"
     */
    private final String snapshotAlgorithm;

    /**
     * Constructor privado. Usar factory method.
     */
    private CronogramaSnapshot(
            CronogramaSnapshotId id,
            ProgramaObraId programaObraId,
            PresupuestoId presupuestoId,
            String fechasJson,
            String duracionesJson,
            String secuenciaJson,
            String calendariosJson,
            LocalDateTime snapshotDate,
            String snapshotAlgorithm) {
        
        validarInvariantes(programaObraId, presupuestoId, fechasJson, duracionesJson, 
                          secuenciaJson, calendariosJson, snapshotAlgorithm);
        
        this.id = Objects.requireNonNull(id, "El ID del snapshot no puede ser nulo");
        this.programaObraId = Objects.requireNonNull(programaObraId, "El programaObraId no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        this.fechasJson = Objects.requireNonNull(fechasJson, "El JSON de fechas no puede ser nulo");
        this.duracionesJson = Objects.requireNonNull(duracionesJson, "El JSON de duraciones no puede ser nulo");
        this.secuenciaJson = Objects.requireNonNull(secuenciaJson, "El JSON de secuencia no puede ser nulo");
        this.calendariosJson = Objects.requireNonNull(calendariosJson, "El JSON de calendarios no puede ser nulo");
        this.snapshotDate = snapshotDate != null ? snapshotDate : LocalDateTime.now();
        this.snapshotAlgorithm = Objects.requireNonNull(snapshotAlgorithm, "El algoritmo de snapshot no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo CronogramaSnapshot.
     * 
     * @param id ID único del snapshot
     * @param programaObraId ID del programa de obra congelado
     * @param presupuestoId ID del presupuesto asociado
     * @param fechasJson JSON con datos de fechas del cronograma
     * @param duracionesJson JSON con datos de duraciones del cronograma
     * @param secuenciaJson JSON con datos de secuencia y dependencias
     * @param calendariosJson JSON con datos de calendarios y restricciones
     * @return Nueva instancia inmutable de CronogramaSnapshot
     * @throws IllegalArgumentException si algún parámetro requerido es nulo o inválido
     */
    public static CronogramaSnapshot crear(
            CronogramaSnapshotId id,
            ProgramaObraId programaObraId,
            PresupuestoId presupuestoId,
            String fechasJson,
            String duracionesJson,
            String secuenciaJson,
            String calendariosJson) {
        
        return new CronogramaSnapshot(
                id,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson,
                LocalDateTime.now(),
                "TEMPORAL-SNAPSHOT-v1"
        );
    }

    /**
     * Factory method para reconstruir un CronogramaSnapshot desde persistencia.
     * 
     * @param id ID único del snapshot
     * @param programaObraId ID del programa de obra congelado
     * @param presupuestoId ID del presupuesto asociado
     * @param fechasJson JSON con datos de fechas del cronograma
     * @param duracionesJson JSON con datos de duraciones del cronograma
     * @param secuenciaJson JSON con datos de secuencia y dependencias
     * @param calendariosJson JSON con datos de calendarios y restricciones
     * @param snapshotDate Fecha y hora en que se creó el snapshot
     * @param snapshotAlgorithm Versión del algoritmo usado
     * @return Instancia inmutable de CronogramaSnapshot reconstruida
     */
    public static CronogramaSnapshot reconstruir(
            CronogramaSnapshotId id,
            ProgramaObraId programaObraId,
            PresupuestoId presupuestoId,
            String fechasJson,
            String duracionesJson,
            String secuenciaJson,
            String calendariosJson,
            LocalDateTime snapshotDate,
            String snapshotAlgorithm) {
        
        return new CronogramaSnapshot(
                id,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson,
                snapshotDate,
                snapshotAlgorithm
        );
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(
            ProgramaObraId programaObraId,
            PresupuestoId presupuestoId,
            String fechasJson,
            String duracionesJson,
            String secuenciaJson,
            String calendariosJson,
            String snapshotAlgorithm) {
        
        if (programaObraId == null) {
            throw new IllegalArgumentException("El programaObraId no puede ser nulo");
        }
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El presupuestoId no puede ser nulo");
        }
        if (fechasJson == null || fechasJson.isBlank()) {
            throw new IllegalArgumentException("El JSON de fechas no puede ser nulo o vacío");
        }
        if (duracionesJson == null || duracionesJson.isBlank()) {
            throw new IllegalArgumentException("El JSON de duraciones no puede ser nulo o vacío");
        }
        if (secuenciaJson == null || secuenciaJson.isBlank()) {
            throw new IllegalArgumentException("El JSON de secuencia no puede ser nulo o vacío");
        }
        if (calendariosJson == null || calendariosJson.isBlank()) {
            throw new IllegalArgumentException("El JSON de calendarios no puede ser nulo o vacío");
        }
        if (snapshotAlgorithm == null || snapshotAlgorithm.isBlank()) {
            throw new IllegalArgumentException("El algoritmo de snapshot no puede ser nulo o vacío");
        }
    }

    // Getters (inmutable - no setters)

    public CronogramaSnapshotId getId() {
        return id;
    }

    public ProgramaObraId getProgramaObraId() {
        return programaObraId;
    }

    public PresupuestoId getPresupuestoId() {
        return presupuestoId;
    }

    public String getFechasJson() {
        return fechasJson;
    }

    public String getDuracionesJson() {
        return duracionesJson;
    }

    public String getSecuenciaJson() {
        return secuenciaJson;
    }

    public String getCalendariosJson() {
        return calendariosJson;
    }

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public String getSnapshotAlgorithm() {
        return snapshotAlgorithm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CronogramaSnapshot that = (CronogramaSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "CronogramaSnapshot{id=%s, programaObraId=%s, presupuestoId=%s, snapshotDate=%s, snapshotAlgorithm=%s}",
                id, programaObraId, presupuestoId, snapshotDate, snapshotAlgorithm
        );
    }
}
