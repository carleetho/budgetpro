package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.exception.InactiveWorkerException;
import com.budgetpro.domain.rrhh.exception.ProyectoNoActivoParaOperacionException;
import com.budgetpro.domain.rrhh.exception.SolapeHorarioTareoException;
import com.budgetpro.domain.rrhh.exception.TrabajadorNoAsignadoAlProyectoException;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Políticas de dominio para registro de tareo (UC-R03): R-02, REGLA-150, REGLA-125 y delegación R-03
 * vía {@link AsignacionSolapeValidator} (sin inventar semántica multi-sitio).
 */
public final class RegistroAsistenciaPolitica {

    private RegistroAsistenciaPolitica() {
    }

    /**
     * R-02: el trabajador debe estar {@link EstadoEmpleado#ACTIVO}.
     */
    public static void validarEmpleadoActivoParaTareo(Empleado empleado) {
        Objects.requireNonNull(empleado, "empleado must not be null");
        if (empleado.getEstado() != EstadoEmpleado.ACTIVO) {
            throw new InactiveWorkerException(empleado.getId(), empleado.getEstado(),
                    String.format("Cannot register attendance: Worker is %s (must be ACTIVO)", empleado.getEstado()));
        }
    }

    /**
     * REGLA-150: solo proyectos en ejecución contractual.
     */
    public static void validarProyectoActivoParaTareo(Proyecto proyecto) {
        Objects.requireNonNull(proyecto, "proyecto must not be null");
        if (proyecto.getEstado() != EstadoProyecto.ACTIVO) {
            ProyectoId id = proyecto.getId();
            throw new ProyectoNoActivoParaOperacionException(id, proyecto.getEstado(), String.format(
                    "Proyecto no activo (id=%s, estado=%s). Solo proyectos ACTIVO permiten registrar asistencia.",
                    id.getValue(), proyecto.getEstado()));
        }
    }

    /**
     * REGLA-125: coherencia entre fecha de tareo y marcas horarias; duración estrictamente positiva.
     */
    public static void validarCoherenciaTemporalTareo(LocalDate fecha, LocalDateTime horaEntrada,
            LocalDateTime horaSalida) {
        Objects.requireNonNull(fecha, "fecha must not be null");
        Objects.requireNonNull(horaEntrada, "horaEntrada must not be null");
        Objects.requireNonNull(horaSalida, "horaSalida must not be null");

        if (!horaEntrada.toLocalDate().equals(fecha)) {
            throw new IllegalArgumentException(
                    "La fecha del tareo debe coincidir con la fecha calendario de hora de entrada.");
        }

        LocalTime tIn = horaEntrada.toLocalTime();
        LocalTime tOut = horaSalida.toLocalTime();
        boolean overnight = tOut.isBefore(tIn);
        if (overnight) {
            if (!horaSalida.toLocalDate().equals(fecha.plusDays(1))) {
                throw new IllegalArgumentException(
                        "Turno nocturno: la hora de salida debe calendariarse al día siguiente al del tareo.");
            }
        } else {
            if (!horaSalida.toLocalDate().equals(fecha)) {
                throw new IllegalArgumentException(
                        "La fecha del tareo debe coincidir con la fecha calendario de hora de salida.");
            }
            if (!tOut.isAfter(tIn)) {
                throw new IllegalArgumentException(
                        "Hora de salida debe ser posterior a hora de entrada en el mismo día calendario.");
            }
        }

        Duration duracion = duracionDesdeMarcas(fecha, tIn, tOut, overnight);
        if (duracion.isZero() || duracion.isNegative()) {
            throw new IllegalArgumentException("La duración del turno debe ser estrictamente positiva.");
        }
    }

    private static Duration duracionDesdeMarcas(LocalDate fecha, LocalTime tIn, LocalTime tOut, boolean overnight) {
        if (overnight) {
            Duration toMidnight = Duration.between(tIn, LocalTime.MAX).plusNanos(1);
            Duration fromMidnight = Duration.between(LocalTime.MIN, tOut);
            return toMidnight.plus(fromMidnight);
        }
        return Duration.between(tIn, tOut);
    }

    /**
     * REGLA-125: el trabajador debe estar asignado al proyecto en la fecha del registro.
     */
    public static void validarAsignacionVigenteAlProyecto(EmpleadoId empleadoId, ProyectoId proyectoId, LocalDate fecha,
            boolean existeAsignacionVigente) {
        Objects.requireNonNull(empleadoId, "empleadoId must not be null");
        Objects.requireNonNull(proyectoId, "proyectoId must not be null");
        Objects.requireNonNull(fecha, "fecha must not be null");
        if (!existeAsignacionVigente) {
            throw new TrabajadorNoAsignadoAlProyectoException(empleadoId, proyectoId, fecha,
                    String.format("El empleado %s no tiene asignación vigente al proyecto %s en la fecha %s.",
                            empleadoId.getValue(), proyectoId.getValue(), fecha));
        }
    }

    /**
     * REGLA-125: no solapes con registros ya persistidos (misma heurística que el repositorio).
     */
    public static void validarSinSolapeConRegistrosExistentes(List<AsistenciaRegistro> solapes) {
        Objects.requireNonNull(solapes, "solapes must not be null");
        if (!solapes.isEmpty()) {
            throw new SolapeHorarioTareoException(
                    "Existen registros de asistencia superpuestos para el empleado en el horario indicado.");
        }
    }

    /**
     * Límite R-03 multi-sitio: delega en el puerto sin implementar semántica propia.
     */
    public static void delegarValidacionSolapeAsignacionR03(AsignacionSolapeValidator asignacionSolapeValidator,
            EmpleadoId empleadoId, LocalDate fecha, Collection<AsignacionProyecto> asignacionesExistentes) {
        Objects.requireNonNull(asignacionSolapeValidator, "asignacionSolapeValidator must not be null");
        Objects.requireNonNull(empleadoId, "empleadoId must not be null");
        Objects.requireNonNull(fecha, "fecha must not be null");
        Objects.requireNonNull(asignacionesExistentes, "asignacionesExistentes must not be null");
        asignacionSolapeValidator.validar(empleadoId, fecha, fecha, asignacionesExistentes);
    }
}
