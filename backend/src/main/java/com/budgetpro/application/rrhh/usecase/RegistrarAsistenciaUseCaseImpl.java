package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.dto.RegistrarAsistenciaCommand;
import com.budgetpro.application.rrhh.exception.AsistenciaSuperpuestaException;
import com.budgetpro.application.rrhh.exception.ProyectoNoActivoException;
import com.budgetpro.application.rrhh.port.in.RegistrarAsistenciaUseCase;
import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ProyectoRepositoryPort;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.rrhh.exception.InactiveWorkerException;
import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RegistrarAsistenciaUseCaseImpl implements RegistrarAsistenciaUseCase {

    private final EmpleadoRepositoryPort empleadoRepositoryPort;
    private final ProyectoRepositoryPort proyectoRepositoryPort;
    private final AsistenciaRepositoryPort asistenciaRepositoryPort;

    public RegistrarAsistenciaUseCaseImpl(EmpleadoRepositoryPort empleadoRepositoryPort,
            ProyectoRepositoryPort proyectoRepositoryPort, AsistenciaRepositoryPort asistenciaRepositoryPort) {
        this.empleadoRepositoryPort = empleadoRepositoryPort;
        this.proyectoRepositoryPort = proyectoRepositoryPort;
        this.asistenciaRepositoryPort = asistenciaRepositoryPort;
    }

    @Override
    public AsistenciaResponse registrarAsistencia(RegistrarAsistenciaCommand command) {
        Empleado empleado = empleadoRepositoryPort.findById(command.getEmpleadoId()).orElseThrow(
                () -> new IllegalArgumentException("Empleado no encontrado: " + command.getEmpleadoId().getValue()));

        if (empleado.getEstado() != EstadoEmpleado.ACTIVO) {
            throw new InactiveWorkerException(command.getEmpleadoId(), empleado.getEstado(),
                    String.format("Cannot register attendance: Worker is %s (must be ACTIVO)", empleado.getEstado()));
        }

        Proyecto proyecto = proyectoRepositoryPort.findById(command.getProyectoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Proyecto no encontrado: " + command.getProyectoId().getValue()));
        if (proyecto.getEstado() != EstadoProyecto.ACTIVO) {
            throw new ProyectoNoActivoException(String.format(
                    "Proyecto no activo (id=%s, estado=%s). Solo proyectos ACTIVO permiten registrar asistencia.",
                    command.getProyectoId().getValue(), proyecto.getEstado()));
        }

        LocalDateTime inicioVentana = LocalDateTime.of(command.getFecha(), command.getHoraEntrada().toLocalTime());
        LocalDateTime finVentana;
        if (command.getHoraSalida().toLocalTime().isBefore(command.getHoraEntrada().toLocalTime())) {
            finVentana = LocalDateTime.of(command.getFecha().plusDays(1), command.getHoraSalida().toLocalTime());
        } else {
            finVentana = LocalDateTime.of(command.getFecha(), command.getHoraSalida().toLocalTime());
        }

        List<AsistenciaRegistro> overlaps = asistenciaRepositoryPort.findOverlapping(command.getEmpleadoId(),
                command.getProyectoId(), inicioVentana, finVentana);

        if (!overlaps.isEmpty()) {
            throw new AsistenciaSuperpuestaException(
                    "Existen registros de asistencia superpuestos para el empleado en el horario indicado.");
        }

        AsistenciaRegistro asistencia = AsistenciaRegistro.registrar(AsistenciaId.random(), command.getEmpleadoId(),
                command.getProyectoId(), command.getFecha(), command.getHoraEntrada().toLocalTime(),
                command.getHoraSalida().toLocalTime(), command.getUbicacion());

        AsistenciaRegistro saved = asistenciaRepositoryPort.save(asistencia);

        LocalDate fechaSalida;
        if (saved.esOvernight()) {
            fechaSalida = saved.getFecha().plusDays(1);
        } else {
            fechaSalida = saved.getFecha();
        }
        LocalDateTime horaSalidaRespuesta = fechaSalida.atTime(saved.getHoraSalida());
        LocalDateTime horaEntradaRespuesta = saved.getFecha().atTime(saved.getHoraEntrada());
        double horasTrabajadas = saved.calcularHoras().toMinutes() / 60.0;
        double horasExtras = saved.calcularHorasExtras().toMinutes() / 60.0;
        return new AsistenciaResponse(saved.getId(), saved.getFecha(), horaEntradaRespuesta, horaSalidaRespuesta, horasTrabajadas, horasExtras);
    }
}
