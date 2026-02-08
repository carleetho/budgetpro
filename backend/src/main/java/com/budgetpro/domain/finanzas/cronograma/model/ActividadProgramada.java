package com.budgetpro.domain.finanzas.cronograma.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad del agregado ProgramaObra que representa una Actividad Programada.
 * 
 * Relación 1:1 con Partida del Presupuesto.
 * 
 * Responsabilidad: - Representar una actividad del cronograma con sus fechas -
 * Gestionar dependencias con otras actividades (Fin-Inicio simple)
 * 
 * Invariantes: - El partidaId es obligatorio - La fechaFin no puede ser menor a
 * fechaInicio - La duracionDias debe ser consistente con las fechas
 */
public final class ActividadProgramada {

    private final ActividadProgramadaId id;
    private final UUID partidaId;
    private final UUID programaObraId;
    // Justificación: Fecha inicio ajustable
    // nosemgrep
    private LocalDate fechaInicio;
    // Justificación: Fecha fin ajustable
    // nosemgrep
    private LocalDate fechaFin;
    // Justificación: Campo calculado automáticamente
    // nosemgrep
    private Integer duracionDias;
    // Justificación: Gestión de dependencias
    // nosemgrep
    private List<UUID> predecesoras;
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private ActividadProgramada(ActividadProgramadaId id, UUID partidaId, UUID programaObraId, LocalDate fechaInicio,
            LocalDate fechaFin, Integer duracionDias, List<UUID> predecesoras, Long version) {
        validarInvariantes(partidaId, programaObraId, fechaInicio, fechaFin);

        this.id = Objects.requireNonNull(id, "El ID de la actividad programada no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El partidaId no puede ser nulo");
        this.programaObraId = Objects.requireNonNull(programaObraId, "El programaObraId no puede ser nulo");
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.duracionDias = calcularDuracion(fechaInicio, fechaFin);
        this.predecesoras = predecesoras != null ? new ArrayList<>(predecesoras) : new ArrayList<>();
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear una nueva ActividadProgramada.
     */
    public static ActividadProgramada crear(ActividadProgramadaId id, UUID partidaId, UUID programaObraId,
            LocalDate fechaInicio, LocalDate fechaFin) {
        return new ActividadProgramada(id, partidaId, programaObraId, fechaInicio, fechaFin, null, null, 0L);
    }

    /**
     * Factory method para reconstruir una ActividadProgramada desde persistencia.
     */
    public static ActividadProgramada reconstruir(ActividadProgramadaId id, UUID partidaId, UUID programaObraId,
            LocalDate fechaInicio, LocalDate fechaFin, Integer duracionDias, List<UUID> predecesoras, Long version) {
        return new ActividadProgramada(id, partidaId, programaObraId, fechaInicio, fechaFin, duracionDias, predecesoras,
                version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID partidaId, UUID programaObraId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (partidaId == null) {
            throw new IllegalArgumentException("El partidaId no puede ser nulo");
        }
        if (programaObraId == null) {
            throw new IllegalArgumentException("El programaObraId no puede ser nulo");
        }
        if (fechaInicio != null && fechaFin != null) {
            if (fechaFin.isBefore(fechaInicio)) {
                throw new IllegalArgumentException("La fecha de fin no puede ser menor a la fecha de inicio");
            }
        }
    }

    /**
     * Calcula la duración en días entre dos fechas.
     */
    private Integer calcularDuracion(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return null;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1; // +1 para incluir ambos
                                                                                            // días
    }

    /**
     * Actualiza las fechas de la actividad y recalcula la duración.
     */
    public void actualizarFechas(LocalDate nuevaFechaInicio, LocalDate nuevaFechaFin) {
        validarInvariantes(this.partidaId, this.programaObraId, nuevaFechaInicio, nuevaFechaFin);
        this.fechaInicio = nuevaFechaInicio;
        this.fechaFin = nuevaFechaFin;
        this.duracionDias = calcularDuracion(nuevaFechaInicio, nuevaFechaFin);
    }

    /**
     * Agrega una actividad predecesora (dependencia Fin-Inicio).
     */
    public void agregarPredecesora(UUID actividadPredecesoraId) {
        if (actividadPredecesoraId == null) {
            throw new IllegalArgumentException("El ID de la actividad predecesora no puede ser nulo");
        }
        if (actividadPredecesoraId.equals(this.id.getValue())) {
            throw new IllegalArgumentException("Una actividad no puede ser predecesora de sí misma");
        }
        if (!this.predecesoras.contains(actividadPredecesoraId)) {
            this.predecesoras.add(actividadPredecesoraId);
        }
    }

    /**
     * Elimina una actividad predecesora.
     */
    public void eliminarPredecesora(UUID actividadPredecesoraId) {
        this.predecesoras.remove(actividadPredecesoraId);
    }

    // Getters

    public ActividadProgramadaId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public UUID getProgramaObraId() {
        return programaObraId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public List<UUID> getPredecesoras() {
        return Collections.unmodifiableList(predecesoras);
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ActividadProgramada that = (ActividadProgramada) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ActividadProgramada{id=%s, partidaId=%s, fechaInicio=%s, fechaFin=%s, duracionDias=%d}",
                id, partidaId, fechaInicio, fechaFin, duracionDias);
    }
}
