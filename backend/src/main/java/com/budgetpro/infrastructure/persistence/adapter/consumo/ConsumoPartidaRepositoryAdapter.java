package com.budgetpro.infrastructure.persistence.adapter.consumo;

import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartidaId;
import com.budgetpro.domain.finanzas.consumo.port.out.ConsumoPartidaRepository;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.consumo.ConsumoPartidaEntity;
import com.budgetpro.infrastructure.persistence.mapper.consumo.ConsumoPartidaMapper;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.consumo.ConsumoPartidaJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para ConsumoPartidaRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ConsumoPartidaRepositoryAdapter implements ConsumoPartidaRepository {

    private final ConsumoPartidaJpaRepository jpaRepository;
    private final PartidaJpaRepository partidaJpaRepository;
    private final ConsumoPartidaMapper mapper;

    public ConsumoPartidaRepositoryAdapter(ConsumoPartidaJpaRepository jpaRepository,
                                          PartidaJpaRepository partidaJpaRepository,
                                          ConsumoPartidaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.partidaJpaRepository = partidaJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(ConsumoPartida consumo) {
        // Cargar la partida relacionada
        PartidaEntity partidaEntity = partidaJpaRepository.findById(consumo.getPartidaId())
                .orElseThrow(() -> new IllegalStateException("Partida no encontrada: " + consumo.getPartidaId()));

        Optional<ConsumoPartidaEntity> existingEntityOpt = jpaRepository.findById(consumo.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            ConsumoPartidaEntity existingEntity = existingEntityOpt.get();
            existingEntity.setMonto(consumo.getMonto());
            existingEntity.setFecha(consumo.getFecha());
            existingEntity.setTipo(consumo.getTipo());
            existingEntity.setCompraDetalleId(consumo.getCompraDetalleId());
            // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            ConsumoPartidaEntity newEntity = mapper.toEntity(consumo, partidaEntity);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional
    public void saveAll(List<ConsumoPartida> consumos) {
        for (ConsumoPartida consumo : consumos) {
            save(consumo);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsumoPartida> findById(ConsumoPartidaId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumoPartida> findByPartidaId(UUID partidaId) {
        return jpaRepository.findByPartidaId(partidaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumoPartida> findByCompraDetalleId(UUID compraDetalleId) {
        return jpaRepository.findByCompraDetalleId(compraDetalleId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
