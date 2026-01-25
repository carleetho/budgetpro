package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.dto.RegistrarAsistenciaCommand;
import com.budgetpro.application.rrhh.exception.AsistenciaSuperpuestaException;
import com.budgetpro.application.rrhh.port.in.RegistrarAsistenciaUseCase;
import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ProyectoRepositoryPort;
import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
        if (empleadoRepositoryPort.findById(command.getEmpleadoId()).isEmpty()) {
            throw new IllegalArgumentException("Empleado no encontrado: " + command.getEmpleadoId().getValue());
        }

        if (!proyectoRepositoryPort.existsById(command.getProyectoId())) {
            throw new IllegalArgumentException("Proyecto no encontrado: " + command.getProyectoId().getValue());
        }

        List<AsistenciaRegistro> overlaps = asistenciaRepositoryPort.findOverlapping(command.getEmpleadoId(),
                command.getHoraEntrada(), command.getHoraSalida());

        if (!overlaps.isEmpty()) {
            throw new AsistenciaSuperpuestaException(
                    "Existen registros de asistencia superpuestos para el empleado en el horario indicado.");
        }

        AsistenciaRegistro asistencia = AsistenciaRegistro.registrar(AsistenciaId.random(), command.getEmpleadoId(),
                command.getProyectoId(), command.getFecha(), command.getHoraEntrada().toLocalTime(),
                command.getHoraSalida().toLocalTime(), command.getUbicacion());

        AsistenciaRegistro saved = asistenciaRepositoryPort.save(asistencia);

        return new AsistenciaResponse(saved.getId(), saved.getFecha(), saved.getFecha().atTime(saved.getHoraEntrada()),
                saved.esOvernight() ? saved.getFecha().plusDays(1).atTime(saved.getHoraSalida())
                        : saved.getFecha().atTime(saved.getHoraSalida()),
                saved.calcularHoras().toMinutes() / 60.0, saved.calcularHorasExtras().toMinutes() / 60.0);
    }
}
