package com.budgetpro.domain.rrhh.model;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class AsignacionActividad {
    private final UUID id;
    private final CuadrillaId cuadrillaId;
    private final ProyectoId proyectoId;
    private final UUID partidaId; // Assuming simply UUID as PartidaId value object not available yet
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;

    private AsignacionActividad(UUID id, CuadrillaId cuadrillaId, ProyectoId proyectoId, UUID partidaId,
            LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.cuadrillaId = Objects.requireNonNull(cuadrillaId, "CuadrillaId cannot be null");
        this.proyectoId = Objects.requireNonNull(proyectoId, "ProyectoId cannot be null");
        this.partidaId = Objects.requireNonNull(partidaId, "PartidaId cannot be null");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "FechaInicio cannot be null");
        this.fechaFin = Objects.requireNonNull(fechaFin, "FechaFin cannot be null");

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("FechaFin cannot be before FechaInicio");
        }
    }

    public static AsignacionActividad crear(CuadrillaId cuadrillaId, ProyectoId proyectoId, UUID partidaId,
            LocalDate fechaInicio, LocalDate fechaFin) {
        return new AsignacionActividad(UUID.randomUUID(), cuadrillaId, proyectoId, partidaId, fechaInicio, fechaFin);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public CuadrillaId getCuadrillaId() {
        return cuadrillaId;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }
}
