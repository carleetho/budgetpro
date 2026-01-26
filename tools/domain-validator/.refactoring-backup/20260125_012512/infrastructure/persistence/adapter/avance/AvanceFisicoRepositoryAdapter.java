package com.budgetpro.infrastructure.persistence.adapter.avance;

import com.budgetpro.domain.finanzas.avance.model.AvanceFisico;
import com.budgetpro.domain.finanzas.avance.model.AvanceFisicoId;
import com.budgetpro.domain.finanzas.avance.port.out.AvanceFisicoRepository;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.avance.AvanceFisicoEntity;
import com.budgetpro.infrastructure.persistence.mapper.avance.AvanceFisicoMapper;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.avance.AvanceFisicoJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para AvanceFisicoRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class AvanceFisicoRepositoryAdapter implements AvanceFisicoRepository {

    private final AvanceFisicoJpaRepository jpaRepository;
    private final PartidaJpaRepository partidaJpaRepository;
    private final AvanceFisicoMapper mapper;

    public AvanceFisicoRepositoryAdapter(AvanceFisicoJpaRepository jpaRepository,
                                        PartidaJpaRepository partidaJpaRepository,
                                        AvanceFisicoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.partidaJpaRepository = partidaJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(AvanceFisico avance) {
        Optional<AvanceFisicoEntity> existingEntityOpt = jpaRepository.findById(avance.getId().getValue());
        
        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos
            AvanceFisicoEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, avance);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            PartidaEntity partidaEntity = partidaJpaRepository.findById(avance.getPartidaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Partida no encontrada: " + avance.getPartidaId()));
            
            AvanceFisicoEntity newEntity = mapper.toEntity(avance, partidaEntity);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AvanceFisico> findById(AvanceFisicoId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvanceFisico> findByPartidaId(UUID partidaId) {
        return jpaRepository.findByPartidaId(partidaId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvanceFisico> findByPartidaIdAndFechaBetween(UUID partidaId, LocalDate fechaInicio, LocalDate fechaFin) {
        return jpaRepository.findByPartidaIdAndFechaBetween(partidaId, fechaInicio, fechaFin).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
