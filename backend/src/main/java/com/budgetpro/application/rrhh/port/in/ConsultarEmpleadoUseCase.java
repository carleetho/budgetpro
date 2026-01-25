package com.budgetpro.application.rrhh.port.in;

import com.budgetpro.application.rrhh.dto.EmpleadoResponse;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;

import java.util.List;
import java.util.Optional;

public interface ConsultarEmpleadoUseCase {
    Optional<EmpleadoResponse> findById(String id);

    List<EmpleadoResponse> findByEstado(EstadoEmpleado estado);

    List<EmpleadoResponse> findAll();
}
