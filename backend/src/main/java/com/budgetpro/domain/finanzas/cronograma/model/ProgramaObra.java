package com.budgetpro.domain.finanzas.cronograma.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raíz que representa el Programa de Obra de un Proyecto.
 * 
 * Relación 1:1 con Proyecto.
 * 
 * Responsabilidad:
 * - Gestionar la programación temporal del proyecto
 * - Calcular la duración total del proyecto
 * - Coordinar las actividades programadas
 * 
 * Invariantes:
 * - El proyectoId es obligatorio
 * - La fechaFinEstimada no puede ser menor a fechaInicio
 * - La duracionTotalDias debe ser consistente con las fechas
 */
public final class ProgramaObra {

    private final ProgramaObraId id;
    private final UUID proyectoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private Integer duracionTotalDias; // Calculada: diferencia entre fechaInicio y fechaFinEstimada
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private ProgramaObra(ProgramaObraId id, UUID proyectoId, LocalDate fechaInicio,
                        LocalDate fechaFinEstimada, Integer duracionTotalDias, Long version) {
        validarInvariantes(proyectoId, fechaInicio, fechaFinEstimada);
        
        this.id = Objects.requireNonNull(id, "El ID del programa de obra no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.duracionTotalDias = calcularDuracion(fechaInicio, fechaFinEstimada);
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear un nuevo ProgramaObra.
     */
    public static ProgramaObra crear(ProgramaObraId id, UUID proyectoId, LocalDate fechaInicio, LocalDate fechaFinEstimada) {
        return new ProgramaObra(id, proyectoId, fechaInicio, fechaFinEstimada, null, 0L);
    }

    /**
     * Factory method para reconstruir un ProgramaObra desde persistencia.
     */
    public static ProgramaObra reconstruir(ProgramaObraId id, UUID proyectoId, LocalDate fechaInicio,
                                          LocalDate fechaFinEstimada, Integer duracionTotalDias, Long version) {
        return new ProgramaObra(id, proyectoId, fechaInicio, fechaFinEstimada, duracionTotalDias, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, LocalDate fechaInicio, LocalDate fechaFinEstimada) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (fechaInicio != null && fechaFinEstimada != null) {
            if (fechaFinEstimada.isBefore(fechaInicio)) {
                throw new IllegalArgumentException("La fecha de fin estimada no puede ser menor a la fecha de inicio");
            }
        }
    }

    /**
     * Calcula la duración en días entre dos fechas.
     */
    private Integer calcularDuracion(LocalDate fechaInicio, LocalDate fechaFinEstimada) {
        if (fechaInicio == null || fechaFinEstimada == null) {
            return null;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFinEstimada) + 1; // +1 para incluir ambos días
    }

    /**
     * Actualiza las fechas del programa y recalcula la duración.
     */
    public void actualizarFechas(LocalDate nuevaFechaInicio, LocalDate nuevaFechaFinEstimada) {
        validarInvariantes(this.proyectoId, nuevaFechaInicio, nuevaFechaFinEstimada);
        this.fechaInicio = nuevaFechaInicio;
        this.fechaFinEstimada = nuevaFechaFinEstimada;
        this.duracionTotalDias = calcularDuracion(nuevaFechaInicio, nuevaFechaFinEstimada);
    }

    /**
     * Actualiza la fecha de fin estimada basándose en la fecha de fin más tardía de las actividades.
     * 
     * @param fechaFinMasTardia La fecha de fin más tardía de todas las actividades
     */
    public void actualizarFechaFinDesdeActividades(LocalDate fechaFinMasTardia) {
        if (fechaFinMasTardia == null) {
            return;
        }
        if (this.fechaInicio == null) {
            throw new IllegalStateException("No se puede actualizar la fecha de fin sin fecha de inicio");
        }
        if (fechaFinMasTardia.isBefore(this.fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin más tardía no puede ser menor a la fecha de inicio");
        }
        this.fechaFinEstimada = fechaFinMasTardia;
        this.duracionTotalDias = calcularDuracion(this.fechaInicio, this.fechaFinEstimada);
    }

    // Getters

    public ProgramaObraId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public Integer getDuracionTotalDias() {
        return duracionTotalDias;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramaObra that = (ProgramaObra) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ProgramaObra{id=%s, proyectoId=%s, fechaInicio=%s, fechaFinEstimada=%s, duracionTotalDias=%d}", 
                           id, proyectoId, fechaInicio, fechaFinEstimada, duracionTotalDias);
    }
}
