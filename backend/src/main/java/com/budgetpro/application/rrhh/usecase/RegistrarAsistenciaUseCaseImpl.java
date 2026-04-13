package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.dto.RegistrarAsistenciaCommand;
import com.budgetpro.application.rrhh.exception.AsistenciaSuperpuestaException;
import com.budgetpro.application.rrhh.exception.ProyectoNoActivoException;
import com.budgetpro.application.rrhh.port.in.RegistrarAsistenciaUseCase;
import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ProyectoRepositoryPort;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.rrhh.exception.ProyectoNoActivoParaOperacionException;
import com.budgetpro.domain.rrhh.exception.SolapeHorarioTareoException;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.port.AsignacionSolapeValidator;
import com.budgetpro.domain.rrhh.service.RegistroAsistenciaPolitica;
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
    private final AsignacionProyectoRepositoryPort asignacionProyectoRepositoryPort;
    private final AsignacionSolapeValidator asignacionSolapeValidator;

    public RegistrarAsistenciaUseCaseImpl(EmpleadoRepositoryPort empleadoRepositoryPort,
            ProyectoRepositoryPort proyectoRepositoryPort, AsistenciaRepositoryPort asistenciaRepositoryPort,
            AsignacionProyectoRepositoryPort asignacionProyectoRepositoryPort,
            AsignacionSolapeValidator asignacionSolapeValidator) {
        this.empleadoRepositoryPort = empleadoRepositoryPort;
        this.proyectoRepositoryPort = proyectoRepositoryPort;
        this.asistenciaRepositoryPort = asistenciaRepositoryPort;
        this.asignacionProyectoRepositoryPort = asignacionProyectoRepositoryPort;
        this.asignacionSolapeValidator = asignacionSolapeValidator;
    }

    @Override
    public AsistenciaResponse registrarAsistencia(RegistrarAsistenciaCommand command) {
        Empleado empleado = empleadoRepositoryPort.findById(command.getEmpleadoId()).orElseThrow(
                () -> new IllegalArgumentException("Empleado no encontrado: " + command.getEmpleadoId().getValue()));

        RegistroAsistenciaPolitica.validarEmpleadoActivoParaTareo(empleado);

        Proyecto proyecto = proyectoRepositoryPort.findById(command.getProyectoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Proyecto no encontrado: " + command.getProyectoId().getValue()));
        try {
            RegistroAsistenciaPolitica.validarProyectoActivoParaTareo(proyecto);
        } catch (ProyectoNoActivoParaOperacionException e) {
            throw new ProyectoNoActivoException(e.getMessage());
        }

        RegistroAsistenciaPolitica.validarCoherenciaTemporalTareo(command.getFecha(), command.getHoraEntrada(),
                command.getHoraSalida());

        boolean asignado = asignacionProyectoRepositoryPort.existsVigenteAsignacionEmpleadoProyectoEnFecha(
                command.getEmpleadoId(), command.getProyectoId(), command.getFecha());
        RegistroAsistenciaPolitica.validarAsignacionVigenteAlProyecto(command.getEmpleadoId(),
                command.getProyectoId(), command.getFecha(), asignado);

        LocalDateTime inicioVentana = LocalDateTime.of(command.getFecha(), command.getHoraEntrada().toLocalTime());
        LocalDateTime finVentana;
        if (command.getHoraSalida().toLocalTime().isBefore(command.getHoraEntrada().toLocalTime())) {
            finVentana = LocalDateTime.of(command.getFecha().plusDays(1), command.getHoraSalida().toLocalTime());
        } else {
            finVentana = LocalDateTime.of(command.getFecha(), command.getHoraSalida().toLocalTime());
        }

        List<AsistenciaRegistro> overlaps = asistenciaRepositoryPort.findOverlapping(command.getEmpleadoId(),
                command.getProyectoId(), inicioVentana, finVentana);

        try {
            RegistroAsistenciaPolitica.validarSinSolapeConRegistrosExistentes(overlaps);
        } catch (SolapeHorarioTareoException e) {
            throw new AsistenciaSuperpuestaException(e.getMessage());
        }

        List<AsignacionProyecto> asignacionesEmpleado = asignacionProyectoRepositoryPort
                .findAsignacionesByEmpleadoId(command.getEmpleadoId());
        RegistroAsistenciaPolitica.delegarValidacionSolapeAsignacionR03(asignacionSolapeValidator,
                command.getEmpleadoId(), command.getFecha(), asignacionesEmpleado);

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
