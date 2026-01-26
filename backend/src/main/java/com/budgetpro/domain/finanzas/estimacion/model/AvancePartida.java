package com.budgetpro.domain.finanzas.estimacion.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa el registro histórico de avance de una partida. Se
 * genera/actualiza cuando se aprueba una estimación.
 */
public class AvancePartida {

    private final AvancePartidaId id;
    private final UUID partidaId;
    private final EstimacionId estimacionId;
    private final LocalDateTime fechaRegistro;
    private final PorcentajeAvance porcentajeAvance;
    private final MontoEstimado montoAcumulado;

    private AvancePartida(AvancePartidaId id, UUID partidaId, EstimacionId estimacionId, LocalDateTime fechaRegistro,
            PorcentajeAvance porcentajeAvance, MontoEstimado montoAcumulado) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El ID de la partida no puede ser nulo");
        this.estimacionId = Objects.requireNonNull(estimacionId, "El ID de la estimación no puede ser nulo");
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro, "La fecha de registro no puede ser nula");
        this.porcentajeAvance = Objects.requireNonNull(porcentajeAvance, "El porcentaje de avance no puede ser nulo");
        this.montoAcumulado = Objects.requireNonNull(montoAcumulado, "El monto acumulado no puede ser nulo");
    }

    public static AvancePartida registrar(UUID partidaId, EstimacionId estimacionId, PorcentajeAvance porcentajeAvance,
            MontoEstimado montoAcumulado) {
        return new AvancePartida(AvancePartidaId.random(), partidaId, estimacionId, LocalDateTime.now(),
                porcentajeAvance, montoAcumulado);
    }

    public static AvancePartida reconstruir(AvancePartidaId id, UUID partidaId, EstimacionId estimacionId,
            LocalDateTime fechaRegistro, PorcentajeAvance porcentajeAvance, MontoEstimado montoAcumulado) {
        return new AvancePartida(id, partidaId, estimacionId, fechaRegistro, porcentajeAvance, montoAcumulado);
    }

    public AvancePartidaId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public EstimacionId getEstimacionId() {
        return estimacionId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public PorcentajeAvance getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public MontoEstimado getMontoAcumulado() {
        return montoAcumulado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AvancePartida that = (AvancePartida) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
