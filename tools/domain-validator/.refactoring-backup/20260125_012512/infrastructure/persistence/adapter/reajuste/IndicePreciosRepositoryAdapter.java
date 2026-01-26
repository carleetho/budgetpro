package com.budgetpro.infrastructure.persistence.adapter.reajuste;

import com.budgetpro.domain.finanzas.reajuste.model.IndicePrecios;
import com.budgetpro.domain.finanzas.reajuste.model.IndicePreciosId;
import com.budgetpro.domain.finanzas.reajuste.port.out.IndicePreciosRepository;
import com.budgetpro.infrastructure.persistence.entity.reajuste.IndicePreciosEntity;
import com.budgetpro.infrastructure.persistence.mapper.reajuste.IndicePreciosMapper;
import com.budgetpro.infrastructure.persistence.repository.reajuste.IndicePreciosJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para IndicePreciosRepository.
 */
@Component
public class IndicePreciosRepositoryAdapter implements IndicePreciosRepository {

    private final IndicePreciosJpaRepository jpaRepository;
    private final IndicePreciosMapper mapper;

    public IndicePreciosRepositoryAdapter(IndicePreciosJpaRepository jpaRepository, IndicePreciosMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void guardar(IndicePrecios indice) {
        Optional<IndicePreciosEntity> existingEntityOpt = jpaRepository.findById(indice.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            IndicePreciosEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, indice);
            jpaRepository.save(existingEntity);
        } else {
            IndicePreciosEntity newEntity = mapper.toEntity(indice);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndicePrecios> buscarPorId(IndicePreciosId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndicePrecios> buscarPorCodigoYFecha(String codigo, LocalDate fechaBase) {
        return jpaRepository.findByCodigoAndFechaBase(codigo, fechaBase)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndicePrecios> buscarIndiceMasCercano(String codigo, LocalDate fecha) {
        return jpaRepository.findIndiceMasCercano(codigo, fecha)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicePrecios> buscarActivosPorCodigo(String codigo) {
        return jpaRepository.findByCodigoAndActivoTrueOrderByFechaBaseDesc(codigo).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
