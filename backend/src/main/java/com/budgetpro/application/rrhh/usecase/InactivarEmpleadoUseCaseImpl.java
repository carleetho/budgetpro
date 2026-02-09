package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.EmpleadoResponse;
import com.budgetpro.application.rrhh.exception.EmpleadoConAsignacionesActivasException;
import com.budgetpro.application.rrhh.exception.EmpleadoNoEncontradoException;
import com.budgetpro.application.rrhh.port.in.InactivarEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
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
public class InactivarEmpleadoUseCaseImpl implements InactivarEmpleadoUseCase {

    private final EmpleadoRepositoryPort empleadoRepository;
    private final AsignacionProyectoRepositoryPort asignacionProyectoRepository;

    public InactivarEmpleadoUseCaseImpl(EmpleadoRepositoryPort empleadoRepository,
            AsignacionProyectoRepositoryPort asignacionProyectoRepository) {
        this.empleadoRepository = empleadoRepository;
        this.asignacionProyectoRepository = asignacionProyectoRepository;
    }

    @Override
    public EmpleadoResponse ejecutar(String id) {
        EmpleadoId empleadoId = EmpleadoId.fromString(id);
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new EmpleadoNoEncontradoException(id));

        if (asignacionProyectoRepository.existsActiveAssignment(empleadoId)) {
            throw new EmpleadoConAsignacionesActivasException(id);
        }

        empleado.inactivar(LocalDate.now());
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
