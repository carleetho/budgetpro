package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.EmpleadoResponse;
import com.budgetpro.application.rrhh.port.in.ConsultarEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.domain.rrhh.model.HistorialLaboral;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional(readOnly = true)
public class ConsultarEmpleadoUseCaseImpl implements ConsultarEmpleadoUseCase {

    private final EmpleadoRepositoryPort empleadoRepository;

    public ConsultarEmpleadoUseCaseImpl(EmpleadoRepositoryPort empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    public Optional<EmpleadoResponse> findById(String id) {
        return empleadoRepository.findById(EmpleadoId.fromString(id)).map(this::mapToResponse);
    }

    @Override
    public List<EmpleadoResponse> findByEstado(EstadoEmpleado estado) {
        return empleadoRepository.findByEstado(estado).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<EmpleadoResponse> findAll() {
        return empleadoRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
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
