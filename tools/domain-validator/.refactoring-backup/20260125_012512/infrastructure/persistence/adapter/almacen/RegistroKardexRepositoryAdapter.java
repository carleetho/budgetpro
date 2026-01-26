package com.budgetpro.infrastructure.persistence.adapter.almacen;

import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;
import com.budgetpro.domain.logistica.almacen.port.out.RegistroKardexRepository;
import com.budgetpro.infrastructure.persistence.entity.almacen.KardexEntity;
import com.budgetpro.infrastructure.persistence.mapper.almacen.KardexMapper;
import com.budgetpro.infrastructure.persistence.repository.almacen.KardexJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para RegistroKardexRepository.
 */
@Component
public class RegistroKardexRepositoryAdapter implements RegistroKardexRepository {

    private final KardexJpaRepository jpaRepository;
    private final KardexMapper mapper;

    public RegistroKardexRepositoryAdapter(KardexJpaRepository jpaRepository, KardexMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void guardar(RegistroKardex registro) {
        // Los registros de Kárdex son inmutables, solo se crean nuevos
        KardexEntity newEntity = mapper.toEntity(registro);
        jpaRepository.save(newEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegistroKardex> buscarUltimoPorAlmacenIdYRecursoId(UUID almacenId, UUID recursoId) {
        return jpaRepository.findUltimoPorAlmacenIdYRecursoId(almacenId, recursoId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroKardex> buscarPorAlmacenIdYRecursoId(UUID almacenId, UUID recursoId) {
        return jpaRepository.findByAlmacenIdAndRecursoIdOrderByFechaMovimientoDesc(almacenId, recursoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
