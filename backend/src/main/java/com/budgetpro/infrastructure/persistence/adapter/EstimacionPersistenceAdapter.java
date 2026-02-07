package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import com.budgetpro.infrastructure.persistence.mapper.estimacion.EstimacionMapper;
import com.budgetpro.infrastructure.persistence.repository.estimacion.EstimacionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EstimacionPersistenceAdapter implements EstimacionRepository {

    private final EstimacionJpaRepository repository;
    private final EstimacionMapper mapper;

    public EstimacionPersistenceAdapter(EstimacionJpaRepository repository, EstimacionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(Estimacion estimacion) {
        EstimacionEntity entity = mapper.toEntity(estimacion);
        repository.save(entity);
    }

    @Override
    public Optional<Estimacion> findById(EstimacionId id) {
        return repository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<Estimacion> findByProyectoId(UUID proyectoId) {
        return repository.findByProyectoIdOrderByNumeroEstimacionAsc(proyectoId).stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId) {
        return repository.obtenerSiguienteNumeroEstimacion(proyectoId);
    }

    @Override
    public List<Estimacion> findAprobadasByProyectoId(UUID proyectoId) {
        return repository.findAprobadasByProyectoId(proyectoId).stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Estimacion> findByProyectoIdAndNumero(UUID proyectoId, Integer numeroEstimacion) {
        return repository.findByProyectoIdAndNumeroEstimacion(proyectoId, numeroEstimacion).map(mapper::toDomain);
    }
}
