package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.CuadrillaRepositoryPort;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.Cuadrilla;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import com.budgetpro.domain.rrhh.model.EstadoCuadrilla;
import com.budgetpro.infrastructure.persistence.entity.rrhh.CuadrillaEntity;
import com.budgetpro.infrastructure.persistence.mapper.rrhh.CuadrillaMapper;
import com.budgetpro.infrastructure.persistence.repository.rrhh.CuadrillaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CuadrillaRepositoryAdapter implements CuadrillaRepositoryPort {

    private final CuadrillaJpaRepository repository;
    private final CuadrillaMapper mapper;

    public CuadrillaRepositoryAdapter(CuadrillaJpaRepository repository, CuadrillaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Cuadrilla save(Cuadrilla cuadrilla) {
        CuadrillaEntity entity = mapper.toEntity(cuadrilla);
        CuadrillaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Cuadrilla> findById(CuadrillaId id) {
        return repository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<Cuadrilla> findByProyectoAndEstado(ProyectoId proyectoId, EstadoCuadrilla estado) {
        return repository.findByProyectoIdAndEstado(proyectoId.getValue(), estado.name()).stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
