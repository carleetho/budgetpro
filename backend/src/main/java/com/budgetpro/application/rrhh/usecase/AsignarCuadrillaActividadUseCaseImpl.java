package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.AsignarCuadrillaCommand;
import com.budgetpro.application.rrhh.exception.CuadrillaInvalidaException;
import com.budgetpro.application.rrhh.port.in.AsignarCuadrillaActividadUseCase;
import com.budgetpro.application.rrhh.port.out.AsignacionActividadRepositoryPort;
import com.budgetpro.application.rrhh.port.out.CuadrillaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ProyectoRepositoryPort;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.AsignacionActividad;
import com.budgetpro.domain.rrhh.model.Cuadrilla;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import com.budgetpro.domain.rrhh.model.EstadoCuadrilla;

public class AsignarCuadrillaActividadUseCaseImpl implements AsignarCuadrillaActividadUseCase {

    private final CuadrillaRepositoryPort cuadrillaRepository;
    private final ProyectoRepositoryPort proyectoRepository;
    private final AsignacionActividadRepositoryPort asignacionRepository;

    public AsignarCuadrillaActividadUseCaseImpl(CuadrillaRepositoryPort cuadrillaRepository,
            ProyectoRepositoryPort proyectoRepository, AsignacionActividadRepositoryPort asignacionRepository) {
        this.cuadrillaRepository = cuadrillaRepository;
        this.proyectoRepository = proyectoRepository;
        this.asignacionRepository = asignacionRepository;
    }

    @Override
    public void asignarCuadrilla(AsignarCuadrillaCommand command) {
        // Validate Crew
        CuadrillaId cuadrillaId = CuadrillaId.of(command.cuadrillaId());
        Cuadrilla cuadrilla = cuadrillaRepository.findById(cuadrillaId)
                .orElseThrow(() -> new CuadrillaInvalidaException("Crew not found: " + command.cuadrillaId()));

        if (cuadrilla.getEstado() != EstadoCuadrilla.ACTIVA) {
            throw new CuadrillaInvalidaException("Crew must be active to be assigned");
        }

        // Validate Project
        ProyectoId proyectoId = ProyectoId.from(command.proyectoId());
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new CuadrillaInvalidaException("Project not found: " + command.proyectoId());
        }

        // Validate Partida (assuming simplified validation as port not available)
        // Ideally: partidaRepository.existsById(new PartidaId(command.partidaId()))

        // Create Assignment
        AsignacionActividad asignacion = AsignacionActividad.crear(cuadrillaId, proyectoId, command.partidaId(),
                command.fechaInicio(), command.fechaFin());

        asignacionRepository.save(asignacion);
    }
}
