package com.budgetpro.domain.finanzas.alertas.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado que representa el resultado de un análisis paramétrico de un
 * presupuesto.
 * 
 * Contiene las alertas generadas y estadísticas del análisis.
 */
public final class AnalisisPresupuesto {

    private final UUID id;
    private final UUID presupuestoId;
    private final LocalDateTime fechaAnalisis;
    private final List<AlertaParametrica> alertas;
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Integer version;

    /**
     * Constructor privado. Usar factory method.
     */
    private AnalisisPresupuesto(UUID id, UUID presupuestoId, LocalDateTime fechaAnalisis,
            List<AlertaParametrica> alertas, Integer version) {
        this.id = Objects.requireNonNull(id, "El ID del análisis no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El ID del presupuesto no puede ser nulo");
        this.fechaAnalisis = fechaAnalisis != null ? fechaAnalisis : LocalDateTime.now();
        this.alertas = alertas != null ? new ArrayList<>(alertas) : new ArrayList<>();
        this.version = version != null ? version : 0;
    }

    /**
     * Factory method para crear un nuevo análisis.
     */
    public static AnalisisPresupuesto crear(UUID presupuestoId) {
        return new AnalisisPresupuesto(UUID.randomUUID(), presupuestoId, LocalDateTime.now(), new ArrayList<>(), 0);
    }

    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static AnalisisPresupuesto reconstruir(UUID id, UUID presupuestoId, LocalDateTime fechaAnalisis,
            List<AlertaParametrica> alertas, Integer version) {
        return new AnalisisPresupuesto(id, presupuestoId, fechaAnalisis, alertas, version);
    }

    /**
     * Agrega una alerta al análisis.
     */
    public void agregarAlerta(AlertaParametrica alerta) {
        if (alerta != null) {
            this.alertas.add(alerta);
        }
    }

    /**
     * Calcula el total de alertas.
     */
    public int getTotalAlertas() {
        return alertas.size();
    }

    /**
     * Calcula el total de alertas críticas.
     */
    public int getTotalAlertasCriticas() {
        return (int) alertas.stream().filter(a -> a.getNivel() == NivelAlerta.CRITICA).count();
    }

    /**
     * Calcula el total de alertas warning.
     */
    public int getTotalAlertasWarning() {
        return (int) alertas.stream().filter(a -> a.getNivel() == NivelAlerta.WARNING).count();
    }

    /**
     * Calcula el total de alertas info.
     */
    public int getTotalAlertasInfo() {
        return (int) alertas.stream().filter(a -> a.getNivel() == NivelAlerta.INFO).count();
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public LocalDateTime getFechaAnalisis() {
        return fechaAnalisis;
    }

    public List<AlertaParametrica> getAlertas() {
        return List.copyOf(alertas);
    }

    public Integer getVersion() {
        return version;
    }

    public void incrementarVersion() {
        this.version = (this.version != null ? this.version : 0) + 1;
    }
}
