package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepositoryPort {
    Empleado save(Empleado empleado);

    Optional<Empleado> findById(EmpleadoId id);

    List<Empleado> findAllById(List<EmpleadoId> ids);

    Optional<Empleado> findByNumeroIdentificacion(String numeroIdentificacion);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    List<Empleado> findByEstado(EstadoEmpleado estado);

    List<Empleado> findAll();
}
