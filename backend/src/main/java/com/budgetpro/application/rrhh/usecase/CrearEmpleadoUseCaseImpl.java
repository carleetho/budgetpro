package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.CrearEmpleadoCommand;
import com.budgetpro.application.rrhh.dto.EmpleadoResponse;
import com.budgetpro.application.rrhh.exception.NumeroIdentificacionDuplicadoException;
import com.budgetpro.application.rrhh.port.in.CrearEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.rrhh.model.Contacto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.HistorialLaboral;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional
public class CrearEmpleadoUseCaseImpl implements CrearEmpleadoUseCase {

    private final EmpleadoRepositoryPort empleadoRepository;

    public CrearEmpleadoUseCaseImpl(EmpleadoRepositoryPort empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public EmpleadoResponse ejecutar(CrearEmpleadoCommand command) {
        // 1. Check for duplicate identification number
        if (empleadoRepository.existsByNumeroIdentificacion(command.numeroIdentificacion())) {
            throw new NumeroIdentificacionDuplicadoException(command.numeroIdentificacion());
        }

        // 2. Create Contacto value object
        Contacto contacto = Contacto.of(command.email(), command.telefono(), command.direccion());

        // 3. Create Empleado aggregate using factory method
        Empleado empleado = Empleado.crear(EmpleadoId.generate(), command.nombre(), command.apellido(),
                command.numeroIdentificacion(), contacto, command.fechaContratacion(), command.salarioInicial(),
                command.puestoInicial(), command.tipo());

        // 4. Save to repository
        Empleado saved = empleadoRepository.save(empleado);

        // 5. Map to response
        return mapToResponse(saved);
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
                current != null ? current.getFechaInicio().toString() : null // Approx for hiring date if initial
        );
    }
}
