package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistrarAsistenciaCommand {
    private final EmpleadoId empleadoId;
    private final ProyectoId proyectoId;
    private final LocalDate fecha;
    private final LocalDateTime horaEntrada;
    private final LocalDateTime horaSalida;
    private final String ubicacion;

    public RegistrarAsistenciaCommand(EmpleadoId empleadoId, ProyectoId proyectoId, LocalDate fecha,
            LocalDateTime horaEntrada, LocalDateTime horaSalida, String ubicacion) {
        this.empleadoId = empleadoId;
        this.proyectoId = proyectoId;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.ubicacion = ubicacion;
    }

    public EmpleadoId getEmpleadoId() {
        return empleadoId;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
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

    public String getUbicacion() {
        return ubicacion;
    }
}
