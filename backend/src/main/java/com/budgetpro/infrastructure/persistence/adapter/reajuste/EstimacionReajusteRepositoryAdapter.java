package com.budgetpro.infrastructure.persistence.adapter.reajuste;

import com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajuste;
import com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajusteId;
import com.budgetpro.domain.finanzas.reajuste.port.out.EstimacionReajusteRepository;
import com.budgetpro.infrastructure.persistence.entity.reajuste.EstimacionReajusteEntity;
import com.budgetpro.infrastructure.persistence.mapper.reajuste.EstimacionReajusteMapper;
import com.budgetpro.infrastructure.persistence.repository.reajuste.EstimacionReajusteJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para EstimacionReajusteRepository.
 */
@Component
public class EstimacionReajusteRepositoryAdapter implements EstimacionReajusteRepository {

    private final EstimacionReajusteJpaRepository jpaRepository;
    private final EstimacionReajusteMapper mapper;

    public EstimacionReajusteRepositoryAdapter(EstimacionReajusteJpaRepository jpaRepository,
                                               EstimacionReajusteMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void guardar(EstimacionReajuste estimacion) {
        Optional<EstimacionReajusteEntity> existingEntityOpt = jpaRepository.findById(estimacion.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            EstimacionReajusteEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, estimacion);
            jpaRepository.save(existingEntity);
        } else {
            EstimacionReajusteEntity newEntity = mapper.toEntity(estimacion);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EstimacionReajuste> buscarPorId(EstimacionReajusteId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstimacionReajuste> buscarPorProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoIdOrderByNumeroEstimacionDesc(proyectoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstimacionReajuste> buscarPorPresupuestoId(UUID presupuestoId) {
        return jpaRepository.findByPresupuestoIdOrderByNumeroEstimacionDesc(presupuestoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId) {
        return jpaRepository.obtenerSiguienteNumeroEstimacion(proyectoId);
    }
}
