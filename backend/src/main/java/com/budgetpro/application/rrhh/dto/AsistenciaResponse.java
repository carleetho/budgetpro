package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.rrhh.model.AsistenciaId;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AsistenciaResponse {
    private final AsistenciaId id;
    private final LocalDate fecha;
    private final LocalDateTime horaEntrada;
    private final LocalDateTime horaSalida;
    private final double horasTrabajadas;
    private final double horasExtras;

    public AsistenciaResponse(AsistenciaId id, LocalDate fecha, LocalDateTime horaEntrada, LocalDateTime horaSalida,
            double horasTrabajadas, double horasExtras) {
        this.id = id;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.horasTrabajadas = horasTrabajadas;
        this.horasExtras = horasExtras;
    }

    public AsistenciaId getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalDateTime getHoraSalida() {
        return horaSalida;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public double getHorasExtras() {
        return horasExtras;
    }
}
