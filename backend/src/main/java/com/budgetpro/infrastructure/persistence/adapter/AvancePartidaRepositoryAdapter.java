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
    public void save(AvancePartida avancePartida) {
        AvancePartidaEntity entity = new AvancePartidaEntity();
        entity.setId(avancePartida.getId().getValue());
        entity.setPartidaId(avancePartida.getPartidaId());
        // We need estimacion entity ref? Or assume ID setting works if we use
        // `estimacionId` mapping?
        // AvancePartidaEntity has ManyToOne to EstimacionEntity.
        // If we don't have the entity instance, we can set ID via
        // proxy/getReferenceById if enabled or if mapped as ID column.
        // But in my Entity definition I used JoinColumn with Entity object.
        // Simple workaround: Create dummy entity with ID or fetch it. Fetching is
        // better for integrity.
        // But for performance, getReferenceById is standard.
        // Let's rely on setting the relationship properly in domain or here.
        // Domain AvancePartida store estimacionId (UUID).

        // Quick fix to allow setting ID without fetching full entity:
        // Use EntityManager.getReference or create a new EstimacionEntity with just ID.
        com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity estRef = new com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity();
        estRef.setId(avancePartida.getEstimacionId());
        entity.setEstimacion(estRef);

        entity.setFechaRegistro(avancePartida.getFechaRegistro());
        entity.setPorcentajeAvance(avancePartida.getPorcentajeAvance().getValue());
        entity.setMontoAcumulado(avancePartida.getMontoAcumulado().getValueForPersistence());

        avancePartidaJpaRepository.save(entity);
    }

    @Override
    public BigDecimal calcularAvanceAcumulado(UUID partidaId) {
        BigDecimal sum = avancePartidaJpaRepository.sumPorcentajeAvanceByPartidaId(partidaId);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
