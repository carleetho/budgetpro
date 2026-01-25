package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.ActualizarCuadrillaCommand;
import com.budgetpro.application.rrhh.dto.CuadrillaResponse;
import com.budgetpro.application.rrhh.exception.CuadrillaInvalidaException;
import com.budgetpro.application.rrhh.port.in.ActualizarCuadrillaUseCase;
import com.budgetpro.application.rrhh.port.out.CuadrillaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.rrhh.model.Cuadrilla;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import com.budgetpro.domain.rrhh.model.CuadrillaMiembroId; // Assuming we need to find member by member ID or employee ID?
import com.budgetpro.domain.rrhh.model.CuadrillaMiembro;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ActualizarCuadrillaUseCaseImpl implements ActualizarCuadrillaUseCase {

    private final CuadrillaRepositoryPort cuadrillaRepository;
    private final EmpleadoRepositoryPort empleadoRepository;

    public ActualizarCuadrillaUseCaseImpl(CuadrillaRepositoryPort cuadrillaRepository,
            EmpleadoRepositoryPort empleadoRepository) {
        this.cuadrillaRepository = cuadrillaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public CuadrillaResponse actualizarCuadrilla(ActualizarCuadrillaCommand command) {
        CuadrillaId cuadrillaId = CuadrillaId.of(command.cuadrillaId());
        Cuadrilla cuadrilla = cuadrillaRepository.findById(cuadrillaId)
                .orElseThrow(() -> new CuadrillaInvalidaException("Crew not found: " + command.cuadrillaId()));

        if (Boolean.TRUE.equals(command.inactivar())) {
            cuadrilla.inactivar();
        }

        if (command.nuevoLiderId() != null) {
            EmpleadoId nuevoLiderId = EmpleadoId.of(command.nuevoLiderId());
            Empleado nuevoLider = empleadoRepository.findById(nuevoLiderId).orElseThrow(
                    () -> new CuadrillaInvalidaException("New leader not found: " + command.nuevoLiderId()));

            if (nuevoLider.getEstado() != EstadoEmpleado.ACTIVO) {
                throw new CuadrillaInvalidaException("New leader must be an active employee");
            }
            cuadrilla.cambiarLider(nuevoLiderId);
        }

        if (command.agregarMiembroId() != null) {
            EmpleadoId empId = EmpleadoId.of(command.agregarMiembroId());
            Empleado member = empleadoRepository.findById(empId).orElseThrow(
                    () -> new CuadrillaInvalidaException("Member to add not found: " + command.agregarMiembroId()));
            if (member.getEstado() != EstadoEmpleado.ACTIVO) {
                throw new CuadrillaInvalidaException("Member to add must be an active employee");
            }
            // Default role "MIEMBRO"
            cuadrilla.agregarMiembro(empId, "MIEMBRO");
        }

        if (command.removerMiembroId() != null) {
            // Command provides UUID, assuming it's EmpleadoId or CuadrillaMiembroId?
            // DTO says "removerMiembroId". Usually removal is by Member ID (binding), but
            // caller might send Employee ID.
            // Cuadrilla.removerMiembro takes CuadrillaMiembroId.
            // If caller sends EmployeeId, we need to find the active member record for that
            // employee.

            // Strategy: assume command.removerMiembroId refers to the Employee's UUID, find
            // the active membership, and remove it.
            // OR assume it is the unique CuadrillaMiembroId.
            // "ActualizarCuadrillaUseCase: Add/remove members".
            // If I use EmpleadoId, it's safer for clients who know "remove John Doe".
            // Let's assume it's EmpleadoId for simplicity unless DTO implies otherwise.
            // Wait, Cuadrilla has `getMiembroActivo(EmpleadoId)`.

            EmpleadoId empIdToRemove = EmpleadoId.of(command.removerMiembroId());
            CuadrillaMiembro miembro = cuadrilla.getMiembroActivo(empIdToRemove)
                    .orElseThrow(() -> new CuadrillaInvalidaException(
                            "Active member not found in crew for employee: " + command.removerMiembroId()));

            cuadrilla.removerMiembro(miembro.getId());
        }

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
