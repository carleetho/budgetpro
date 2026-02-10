package com.budgetpro.domain.finanzas.avance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado AVANCE_FISICO.
 * 
 * Representa un registro de avance físico de una partida en una fecha
 * específica.
 * 
 * Invariantes: - El partidaId es obligatorio - La fecha es obligatoria - El
 // REGLA-142
 * metradoEjecutado no puede ser negativo - (Opcional MVP) El acumulado no
 * debería superar el metrado total de la partida (Alertar, no bloquear)
 * 
 * Contexto: Control de Producción Física
 */
public final class AvanceFisico {

    private final AvanceFisicoId id;
    private final UUID partidaId;
    private final LocalDate fecha;
    private final BigDecimal metradoEjecutado;
    // Justificación: Notas editables sobre el avance
    // nosemgrep
    private String observacion;
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private AvanceFisico(AvanceFisicoId id, UUID partidaId, LocalDate fecha, BigDecimal metradoEjecutado,
            String observacion, Long version) {
        validarInvariantes(partidaId, fecha, metradoEjecutado);

        this.id = Objects.requireNonNull(id, "El ID del avance físico no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El partidaId no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.metradoEjecutado = metradoEjecutado != null ? metradoEjecutado : BigDecimal.ZERO;
        this.observacion = observacion != null ? observacion.trim() : null;
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear un nuevo AvanceFisico.
     */
    public static AvanceFisico crear(AvanceFisicoId id, UUID partidaId, LocalDate fecha, BigDecimal metradoEjecutado,
            String observacion) {
        return new AvanceFisico(id, partidaId, fecha, metradoEjecutado, observacion, 0L);
    }

    /**
     * Factory method para reconstruir un AvanceFisico desde persistencia.
     */
    public static AvanceFisico reconstruir(AvanceFisicoId id, UUID partidaId, LocalDate fecha,
            BigDecimal metradoEjecutado, String observacion, Long version) {
        return new AvanceFisico(id, partidaId, fecha, metradoEjecutado, observacion, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID partidaId, LocalDate fecha, BigDecimal metradoEjecutado) {
        if (partidaId == null) {
            throw new IllegalArgumentException("El partidaId no puede ser nulo");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (metradoEjecutado != null && metradoEjecutado.compareTo(BigDecimal.ZERO) < 0) {
            // REGLA-126
            throw new IllegalArgumentException("El metrado ejecutado no puede ser negativo");
        }
    }

    /**
     * Actualiza la observación.
     */
    public void actualizarObservacion(String nuevaObservacion) {
        this.observacion = nuevaObservacion != null ? nuevaObservacion.trim() : null;
    }

    // Getters

    public AvanceFisicoId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getMetradoEjecutado() {
        return metradoEjecutado;
    }

    public String getObservacion() {
        return observacion;
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
        AvanceFisico that = (AvanceFisico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("AvanceFisico{id=%s, partidaId=%s, fecha=%s, metradoEjecutado=%s}", id, partidaId, fecha,
                metradoEjecutado);
    }
}
