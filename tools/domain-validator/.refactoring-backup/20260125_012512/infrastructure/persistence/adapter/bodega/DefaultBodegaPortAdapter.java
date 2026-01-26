package com.budgetpro.infrastructure.persistence.adapter.bodega;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.infrastructure.persistence.repository.bodega.BodegaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que resuelve la bodega por defecto de un proyecto.
 * Usa la primera bodega activa ordenada por código.
 */
@Component
public class DefaultBodegaPortAdapter implements DefaultBodegaPort {

    private final BodegaJpaRepository bodegaJpaRepository;

    public DefaultBodegaPortAdapter(BodegaJpaRepository bodegaJpaRepository) {
        this.bodegaJpaRepository = bodegaJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BodegaId> getDefaultForProject(UUID proyectoId) {
        return bodegaJpaRepository.findByProyectoIdAndActivaTrueOrderByCodigoAsc(proyectoId).stream()
                .findFirst()
                .map(b -> BodegaId.of(b.getId()));
    }
}
