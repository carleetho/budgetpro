package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.estimacion.model.AvancePartida;
import com.budgetpro.domain.finanzas.estimacion.port.AvancePartidaRepository;
import com.budgetpro.infrastructure.persistence.entity.estimacion.AvancePartidaEntity;
import com.budgetpro.infrastructure.persistence.repository.AvancePartidaJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AvancePartidaRepositoryAdapter implements AvancePartidaRepository {

    private final AvancePartidaJpaRepository avancePartidaJpaRepository;

    public AvancePartidaRepositoryAdapter(AvancePartidaJpaRepository avancePartidaJpaRepository) {
        this.avancePartidaJpaRepository = avancePartidaJpaRepository;
    }

    // Note: Mappers might be needed if full domain object mapping is required.
    // However, the port defines specialized methods.
    // If 'save' requires mapping, we need a simple internal mapper or logic.
    // AvancePartida is an entity in domain.

    @Override
    public AvancePartida save(AvancePartida avancePartida) {
        AvancePartidaEntity entity = new AvancePartidaEntity();
        entity.setId(avancePartida.getId().getValue());
        entity.setPartidaId(avancePartida.getPartidaId());

        com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity estRef = new com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity();
        estRef.setId(avancePartida.getEstimacionId().getValue());
        entity.setEstimacion(estRef);

        entity.setFechaRegistro(avancePartida.getFechaRegistro());
        entity.setPorcentajeAvance(avancePartida.getPorcentajeAvance().getValue());
        entity.setMontoAcumulado(avancePartida.getMontoAcumulado().getValueForPersistence());

        avancePartidaJpaRepository.save(entity);
        return avancePartida;
    }

    @Override
    public BigDecimal calcularAvanceAcumulado(UUID partidaId) {
        BigDecimal sum = avancePartidaJpaRepository.sumPorcentajeAvanceByPartidaId(partidaId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public java.util.List<AvancePartida> findByPartidaId(UUID partidaId) {
        // TODO: Implement proper mapping from AvancePartidaEntity to AvancePartida
        // For now, return empty list to satisfy compiler
        return java.util.Collections.emptyList();
    }
}
