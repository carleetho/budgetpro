package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.CrearCuadrillaCommand;
import com.budgetpro.application.rrhh.dto.CuadrillaResponse;
import com.budgetpro.application.rrhh.exception.CuadrillaInvalidaException;
import com.budgetpro.application.rrhh.port.in.CrearCuadrillaUseCase;
import com.budgetpro.application.rrhh.port.out.CuadrillaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ProyectoRepositoryPort;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.Cuadrilla;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import com.budgetpro.domain.rrhh.model.CuadrillaMiembro;
import com.budgetpro.domain.rrhh.model.CuadrillaMiembroId;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CrearCuadrillaUseCaseImpl implements CrearCuadrillaUseCase {

    private final CuadrillaRepositoryPort cuadrillaRepository;
    private final EmpleadoRepositoryPort empleadoRepository;
    private final ProyectoRepositoryPort proyectoRepository;

    public CrearCuadrillaUseCaseImpl(CuadrillaRepositoryPort cuadrillaRepository,
            EmpleadoRepositoryPort empleadoRepository, ProyectoRepositoryPort proyectoRepository) {
        this.cuadrillaRepository = cuadrillaRepository;
        this.empleadoRepository = empleadoRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @Override
    public CuadrillaResponse crearCuadrilla(CrearCuadrillaCommand command) {
        // Validate existence of project
        ProyectoId proyectoId = ProyectoId.from(command.proyectoId());
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new CuadrillaInvalidaException("Project not found: " + command.proyectoId());
        }

        // Validate Leader
        EmpleadoId liderId = EmpleadoId.of(command.liderEmpleadoId());
        Empleado lider = empleadoRepository.findById(liderId)
                .orElseThrow(() -> new CuadrillaInvalidaException("Leader not found: " + command.liderEmpleadoId()));

        if (lider.getEstado() != EstadoEmpleado.ACTIVO) {
            throw new CuadrillaInvalidaException("Leader must be an active employee");
        }

        // Validate Members
        List<CuadrillaMiembro> initialMembers = new ArrayList<>();
        if (command.miembrosInicialesIds() != null && !command.miembrosInicialesIds().isEmpty()) {
            for (UUID memberId : command.miembrosInicialesIds()) {
                EmpleadoId empId = EmpleadoId.of(memberId);
                Empleado member = empleadoRepository.findById(empId)
                        .orElseThrow(() -> new CuadrillaInvalidaException("Member not found: " + memberId));

                if (member.getEstado() != EstadoEmpleado.ACTIVO) {
                    throw new CuadrillaInvalidaException("All initial members must be active employees");
                }

                // Assuming default role "MIEMBRO" as command doesn't provide it
                initialMembers
                        .add(CuadrillaMiembro.crear(CuadrillaMiembroId.generate(), empId, "MIEMBRO", LocalDate.now()));
            }
        }

        // Create Cuadrilla
        Cuadrilla cuadrilla = Cuadrilla.crear(CuadrillaId.generate(), proyectoId, command.nombre(), command.tipo(),
                liderId, initialMembers);

        cuadrillaRepository.save(cuadrilla);

        return mapToResponse(cuadrilla);
    }

    private CuadrillaResponse mapToResponse(Cuadrilla cuadrilla) {
        List<UUID> memberIds = cuadrilla.getMiembros().stream().map(m -> m.getEmpleadoId().getValue())
                .collect(Collectors.toList());

        return new CuadrillaResponse(cuadrilla.getId().getValue(), cuadrilla.getProyectoId().getValue(),
                cuadrilla.getNombre(), cuadrilla.getTipo(), cuadrilla.getLiderId().getValue(), cuadrilla.getEstado(),
                memberIds);
    }
}
