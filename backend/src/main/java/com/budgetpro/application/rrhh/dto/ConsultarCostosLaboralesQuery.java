package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.time.LocalDate;
import java.util.Objects;

public class ConsultarCostosLaboralesQuery {

    public enum Agrupacion {
        EMPLEADO, CUADRILLA, PARTIDA
    }

    private final ProyectoId proyectoId;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final Agrupacion agruparPor;
    private final boolean incluirVarianza;

    public ConsultarCostosLaboralesQuery(ProyectoId proyectoId, LocalDate fechaInicio, LocalDate fechaFin,
            Agrupacion agruparPor, boolean incluirVarianza) {
        this.proyectoId = Objects.requireNonNull(proyectoId, "proyectoId must not be null");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "fechaInicio must not be null");
        this.fechaFin = Objects.requireNonNull(fechaFin, "fechaFin must not be null");
        this.agruparPor = Objects.requireNonNull(agruparPor, "agruparPor must not be null");
        this.incluirVarianza = incluirVarianza;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Agrupacion getAgruparPor() {
        return agruparPor;
    }

    public boolean isIncluirVarianza() {
        return incluirVarianza;
    }
}
