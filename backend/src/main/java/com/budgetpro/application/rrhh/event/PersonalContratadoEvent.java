package com.budgetpro.application.rrhh.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de aplicación publicado al persistir un nuevo empleado (contratación).
 */
public class PersonalContratadoEvent {

    private final UUID empleadoId;
    private final String nombre;
    private final String apellido;
    private final String cargo;
    private final String tipo;
    private final LocalDate fechaContratacion;
    private final LocalDateTime timestamp;

    public PersonalContratadoEvent(UUID empleadoId, String nombre, String apellido, String cargo, String tipo,
            LocalDate fechaContratacion, LocalDateTime timestamp) {
        this.empleadoId = empleadoId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cargo = cargo;
        this.tipo = tipo;
        this.fechaContratacion = fechaContratacion;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public UUID getEmpleadoId() {
        return empleadoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCargo() {
        return cargo;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
