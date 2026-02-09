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

        // Update personal data if provided
        if (command.nombre() != null || command.apellido() != null
                || (command.email() != null || command.telefono() != null || command.direccion() != null)) {
            Contacto nuevoContacto = null;
            if (command.email() != null || command.telefono() != null || command.direccion() != null) {
                Contacto actual = empleado.getContacto();
                nuevoContacto = Contacto.crear(
                        command.email() != null ? command.email() : (actual != null ? actual.getEmail() : null),
                        command.telefono() != null ? command.telefono()
                                : (actual != null ? actual.getTelefono() : null),
                        command.direccion() != null ? command.direccion()
                                : (actual != null ? actual.getDireccion() : null));
            }
            empleado = empleado.actualizarDatosPersonales(command.nombre(), command.apellido(), nuevoContacto);
        }

        // Handling Salary/Position updates
        if (command.nuevoSalario() != null || command.nuevoPuesto() != null) {
            LocalDate fecha = command.fechaEfectiva() != null ? command.fechaEfectiva() : LocalDate.now();
            empleado = empleado.actualizarCondicionesLaborales(command.nuevoSalario(), command.nuevoPuesto(), fecha);
        }

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
