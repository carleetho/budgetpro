package com.budgetpro.infrastructure.persistence.adapter.estimacion;

import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import com.budgetpro.infrastructure.persistence.mapper.estimacion.EstimacionMapper;
import com.budgetpro.infrastructure.persistence.repository.estimacion.EstimacionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para EstimacionRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión. Hibernate maneja el
 * Optimistic Locking automáticamente con @Version.
 */
@Component
public class EstimacionRepositoryAdapter implements EstimacionRepository {

    private final EstimacionJpaRepository jpaRepository;
    private final EstimacionMapper mapper;

    public EstimacionRepositoryAdapter(EstimacionJpaRepository jpaRepository, EstimacionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Estimacion save(Estimacion estimacion) {
        Optional<EstimacionEntity> existingEntityOpt = jpaRepository.findById(estimacion.getId().getValue());

        EstimacionEntity savedEntity;
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            EstimacionEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, estimacion);
            savedEntity = jpaRepository.save(existingEntity);
        } else {
            // Creación: nueva entidad
            EstimacionEntity newEntity = mapper.toEntity(estimacion);
            savedEntity = jpaRepository.save(newEntity);
        }

        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Estimacion> findById(EstimacionId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estimacion> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoIdOrderByNumeroEstimacionAsc(proyectoId).stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId) {
        return jpaRepository.obtenerSiguienteNumeroEstimacion(proyectoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estimacion> findAprobadasByProyectoId(UUID proyectoId) {
        return jpaRepository.findAprobadasByProyectoId(proyectoId).stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(EstimacionId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsPeriodoSolapado(UUID proyectoId, java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin) {
        return jpaRepository.existsPeriodoSolapado(proyectoId, fechaInicio, fechaFin);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsPeriodoSolapadoExcludingId(UUID proyectoId, java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin, EstimacionId excludeId) {
        return jpaRepository.existsPeriodoSolapadoExcludingId(proyectoId, fechaInicio, fechaFin, excludeId.getValue());
    }
}
