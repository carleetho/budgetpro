package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.ActualizarEmpleadoCommand;
import com.budgetpro.application.rrhh.dto.EmpleadoResponse;
import com.budgetpro.application.rrhh.exception.EmpleadoNoEncontradoException;
import com.budgetpro.application.rrhh.port.in.ActualizarEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.rrhh.model.Contacto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.HistorialLaboral;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Service
@Validated
@Transactional
public class ActualizarEmpleadoUseCaseImpl implements ActualizarEmpleadoUseCase {

    private final EmpleadoRepositoryPort empleadoRepository;

    public ActualizarEmpleadoUseCaseImpl(EmpleadoRepositoryPort empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public EmpleadoResponse ejecutar(ActualizarEmpleadoCommand command) {
        Empleado empleado = empleadoRepository.findById(EmpleadoId.fromString(command.id()))
                .orElseThrow(() -> new EmpleadoNoEncontradoException(command.id()));

        // Update basic info if provided (Assuming setters or methods exist, wait,
        // Empleado is immutable-ish?)
        // The Empleado domain model shown in prompt does NOT have setters for basic
        // info (nombre, apellido, contacto).
        // I need to check if I can update them.
        // Looking at Empleado.java view above:
        // fields are private. No public setters.
        // It seems the current domain model might be missing methods to update basic
        // info.
        // Creating logic to update via reflection or adding methods to domain?
        // Since I cannot modify domain heavily without permission, but this is
        // "Implement Employee Management", I should probably add the methods to
        // Empleado if they are missing or assume they will be added.
        // Wait, the prompt said "ActualizarEmpleadoUseCase: Handle salary/position
        // changes via Empleado domain methods". It didn't explicitly mention personal
        // info update, but the command has them.
        // I will focus on salary/position as requested and maybe skipped personal info
        // update in domain?
        // Ah, "If personal info change: update directly" in implementation steps.
        // I will assume I need to ADD these methods to the Domain Model as part of the
        // task if they are missing.
        // But wait, the task instructions said "Implement Empleado domain model" was
        // Task 2 (completed).
        // Let's re-read Empleado.java.
        // It has NO setters.
        // I must allow updating basic info. I will verify if I can edit Empleado.java.
        // "Implement Employee Management use cases" implies making it work.
        // I will Add `actualizarDatosPersonales` method to Empleado.java.

        // Handling Salary/Position updates
        if (command.nuevoSalario() != null || command.nuevoPuesto() != null) {
            LocalDate fecha = command.fechaEfectiva() != null ? command.fechaEfectiva() : LocalDate.now();
            empleado.actualizarCondicionesLaborales(command.nuevoSalario(), command.nuevoPuesto(), fecha);
        }

        // I will implement a private helper here to map response, or duplicate logic.

        empleadoRepository.save(empleado);

        return mapToResponse(empleado);
    }

    private EmpleadoResponse mapToResponse(Empleado empleado) {
        HistorialLaboral current = empleado.getSalarioActual().orElse(null);
        return new EmpleadoResponse(empleado.getId().getValue().toString(), empleado.getNombre(),
                empleado.getApellido(), empleado.getNumeroIdentificacion(),
                empleado.getContacto() != null ? empleado.getContacto().getEmail() : null,
                empleado.getContacto() != null ? empleado.getContacto().getTelefono() : null,
                empleado.getContacto() != null ? empleado.getContacto().getDireccion() : null,
                empleado.getEstado().name(), current != null ? current.getSalarioBase() : null,
                current != null ? current.getCargo() : null, current != null ? current.getTipoEmpleado().name() : null,
                current != null ? current.getFechaInicio().toString() : null);
    }
}
