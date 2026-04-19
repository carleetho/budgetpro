package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.mapper.PartidaMapper;
import com.budgetpro.domain.finanzas.presupuesto.model.SubpresupuestoNaming;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.SubpresupuestoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para PartidaRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class PartidaRepositoryAdapter implements PartidaRepository, 
        com.budgetpro.domain.logistica.inventario.port.out.PartidaRepository {

    private final PartidaJpaRepository jpaRepository;
    private final PresupuestoJpaRepository presupuestoJpaRepository;
    private final SubpresupuestoJpaRepository subpresupuestoJpaRepository;
    private final PartidaMapper mapper;

    public PartidaRepositoryAdapter(PartidaJpaRepository jpaRepository,
                                   PresupuestoJpaRepository presupuestoJpaRepository,
                                   SubpresupuestoJpaRepository subpresupuestoJpaRepository,
                                   PartidaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.presupuestoJpaRepository = presupuestoJpaRepository;
        this.subpresupuestoJpaRepository = subpresupuestoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(Partida partida) {
        Optional<PartidaEntity> existingEntityOpt = jpaRepository.findById(partida.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            PartidaEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, partida);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            PresupuestoEntity presupuestoEntity = presupuestoJpaRepository.findById(partida.getPresupuestoId())
                    .orElseThrow(() -> new IllegalStateException("Presupuesto no encontrado: " + partida.getPresupuestoId()));

            var subpresupuestoEntity = subpresupuestoJpaRepository
                    .findByPresupuesto_IdAndNombre(presupuestoEntity.getId(), SubpresupuestoNaming.PRINCIPAL)
                    .orElseThrow(() -> new IllegalStateException(
                            "Subpresupuesto Principal no existe para presupuesto: " + presupuestoEntity.getId()));

            PartidaEntity padreEntity = null;
            if (partida.getPadreId() != null) {
                padreEntity = jpaRepository.findById(partida.getPadreId())
                        .orElseThrow(() -> new IllegalStateException("Partida padre no encontrada: " + partida.getPadreId()));
            }

            PartidaEntity newEntity = mapper.toEntity(partida, presupuestoEntity, subpresupuestoEntity, padreEntity);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Partida> findById(PartidaId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findByPresupuestoId(UUID presupuestoId) {
        return jpaRepository.findByPresupuestoId(presupuestoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Partida> findById(UUID partidaId) {
        return jpaRepository.findById(partidaId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID partidaId) {
        return jpaRepository.existsById(partidaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPresupuestoCongelado(UUID partidaId) {
        return jpaRepository.findById(partidaId)
                .map(partida -> partida.getPresupuesto().getEstado() == EstadoPresupuesto.CONGELADO)
                .orElse(false);
    }
}
