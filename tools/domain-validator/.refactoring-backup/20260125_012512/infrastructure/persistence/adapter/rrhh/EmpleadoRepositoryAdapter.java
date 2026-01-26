package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.mapper.EmpleadoMapper;
import com.budgetpro.infrastructure.persistence.repository.rrhh.EmpleadoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmpleadoRepositoryAdapter implements EmpleadoRepositoryPort {

    private final EmpleadoJpaRepository repository;
    private final EmpleadoMapper mapper;

    public EmpleadoRepositoryAdapter(EmpleadoJpaRepository repository, EmpleadoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Empleado save(Empleado empleado) {
        EmpleadoEntity entity = mapper.toEntity(empleado);
        EmpleadoEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Empleado> findById(EmpleadoId id) {
        return repository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<Empleado> findAllById(List<EmpleadoId> ids) {
        List<java.util.UUID> uuidList = ids.stream().map(EmpleadoId::getValue).collect(Collectors.toList());
        return repository.findAllById(uuidList).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Empleado> findByNumeroIdentificacion(String numeroIdentificacion) {
        return repository.findByNumeroIdentificacion(numeroIdentificacion).map(mapper::toDomain);
    }

    @Override
    public boolean existsByNumeroIdentificacion(String numeroIdentificacion) {
        return repository.existsByNumeroIdentificacion(numeroIdentificacion);
    }

    @Override
    public List<Empleado> findByEstado(EstadoEmpleado estado) {
        return repository.findByEstado(estado).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Empleado> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
