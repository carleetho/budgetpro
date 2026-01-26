package com.budgetpro.domain.rrhh.model;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * AsistenciaRegistro entity representing a daily attendance record. Handles
 * normal and overnight shifts, hours calculation, and overlap detection.
 */
public class AsistenciaRegistro {

    private final AsistenciaId id;
    private final EmpleadoId empleadoId;
    private final ProyectoId proyectoId;
    private final LocalDate fecha;
    private final LocalTime horaEntrada;
    private final LocalTime horaSalida;
    private final String ubicacion;
    private final EstadoAsistencia estado;

    // Private constructor for factory
    private AsistenciaRegistro(AsistenciaId id, EmpleadoId empleadoId, ProyectoId proyectoId, LocalDate fecha,
            LocalTime horaEntrada, LocalTime horaSalida, String ubicacion, EstadoAsistencia estado) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.empleadoId = Objects.requireNonNull(empleadoId, "empleadoId must not be null");
        this.proyectoId = Objects.requireNonNull(proyectoId, "proyectoId must not be null");
        this.fecha = Objects.requireNonNull(fecha, "fecha must not be null");
        this.horaEntrada = Objects.requireNonNull(horaEntrada, "horaEntrada must not be null");
        this.horaSalida = Objects.requireNonNull(horaSalida, "horaSalida must not be null");
        this.ubicacion = ubicacion; // Can be null? requirements didn't specify, assuming existing domain rules or
                                    // nullable.
        this.estado = Objects.requireNonNull(estado, "estado must not be null");
    }

    /**
     * Factory method to create a new attendance record.
     */
    public static AsistenciaRegistro registrar(AsistenciaId id, EmpleadoId empleadoId, ProyectoId proyectoId,
            LocalDate fecha, LocalTime horaEntrada, LocalTime horaSalida, String ubicacion) {
        // Validation logic can go here if needed beyond null checks
        return new AsistenciaRegistro(id, empleadoId, proyectoId, fecha, horaEntrada, horaSalida, ubicacion,
                EstadoAsistencia.PRESENTE);
        // Initial state is PRESENTE upon registration? Requirements listed states but
        // not transition logic.
        // "registrar" implies creating a new record, usually PRESENTE.
    }

    public Duration calcularHoras() {
        if (esOvernight()) {
            Duration toMidnight = Duration.between(horaEntrada, LocalTime.MAX).plusNanos(1); // until 24:00
            Duration fromMidnight = Duration.between(LocalTime.MIN, horaSalida);
            return toMidnight.plus(fromMidnight);
        } else {
            return Duration.between(horaEntrada, horaSalida);
        }
    }

    public Duration calcularHorasExtras() {
        Duration trabajadas = calcularHoras();
        Duration standard = Duration.ofHours(8);
        if (trabajadas.compareTo(standard) > 0) {
            return trabajadas.minus(standard);
        }
        return Duration.ZERO;
    }

    /**
     * Checks if the shift crosses midnight (horaSalida < horaEntrada). Note:
     * "horaSalida < horaEntrada" strictly means overnight because a shift cannot
     * end before it starts on the same day.
     */
    public boolean esOvernight() {
        return horaSalida.isBefore(horaEntrada);
    }

    /**
     * Detects if this attendance record overlaps with another. Considers date and
     * time, including overnight shifts.
     */
    public boolean detectOverlap(AsistenciaRegistro other) {
        if (!this.empleadoId.equals(other.empleadoId)) {
            return false;
        }

        LocalDateTime thisStart = this.getStartDateTime();
        LocalDateTime thisEnd = this.getEndDateTime();

        LocalDateTime otherStart = other.getStartDateTime();
        LocalDateTime otherEnd = other.getEndDateTime();

        // Overlap condition: StartA < EndB && StartB < EndA
        return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
    }

    private LocalDateTime getStartDateTime() {
        return LocalDateTime.of(fecha, horaEntrada);
    }

    private LocalDateTime getEndDateTime() {
        if (esOvernight()) {
            return LocalDateTime.of(fecha.plusDays(1), horaSalida);
        } else {
            return LocalDateTime.of(fecha, horaSalida);
        }
    }

    // Getters

    public AsistenciaId getId() {
        return id;
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

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public EstadoAsistencia getEstado() {
        return estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AsistenciaRegistro that = (AsistenciaRegistro) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
