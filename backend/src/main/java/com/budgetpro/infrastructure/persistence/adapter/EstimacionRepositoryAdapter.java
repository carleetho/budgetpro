package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.EstimacionRepository;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import com.budgetpro.infrastructure.persistence.mapper.EstimacionMapper;
import com.budgetpro.infrastructure.persistence.repository.EstimacionJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EstimacionRepositoryAdapter implements EstimacionRepository {

    private final EstimacionJpaRepository estimacionJpaRepository;
    private final EstimacionMapper estimacionMapper;

    public EstimacionRepositoryAdapter(EstimacionJpaRepository estimacionJpaRepository,
            EstimacionMapper estimacionMapper) {
        this.estimacionJpaRepository = estimacionJpaRepository;
        this.estimacionMapper = estimacionMapper;
    }

    @Override
    public Estimacion save(Estimacion estimacion) {
        EstimacionEntity entity = estimacionMapper.toEntity(estimacion);
        EstimacionEntity savedEntity = estimacionJpaRepository.save(entity);
        return estimacionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Estimacion> findById(EstimacionId id) {
        return estimacionJpaRepository.findById(id.getValue()).map(estimacionMapper::toDomain);
    }

    @Override
    public List<Estimacion> findByProyectoId(UUID proyectoId) {
        return estimacionJpaRepository.findByProyectoId(proyectoId).stream().map(estimacionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Estimacion> findByProyectoIdAndEstado(UUID proyectoId, EstadoEstimacion estado) {
        return estimacionJpaRepository.findByProyectoIdAndEstado(proyectoId, estado).stream()
                .map(estimacionMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void delete(EstimacionId id) {
        estimacionJpaRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsPeriodoSolapado(UUID proyectoId, LocalDate inicio, LocalDate fin) {
        return estimacionJpaRepository.existsPeriodoSolapado(proyectoId, inicio, fin);
    }

    @Override
    public boolean existsPeriodoSolapadoExcludingId(UUID proyectoId, LocalDate inicio, LocalDate fin,
            EstimacionId excludeId) {
        return estimacionJpaRepository.existsPeriodoSolapadoExcludingId(proyectoId, inicio, fin, excludeId.getValue());
    }

    @Override
    public Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId) {
        // Simple implementation: count + 1 or max + 1.
        // Assuming we want strict sequential numbers per project.
        // For MVP/Refactor, using Count + 1 is risky if deletions happen, but
        // acceptable if specified?
        // Better: Max(numero) + 1.
        Integer max = estimacionJpaRepository.findMaxNumeroEstimacionByProyectoId(proyectoId);
        return max != null ? max + 1 : 1;
    }
}
