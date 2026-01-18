package com.budgetpro.infrastructure.persistence.adapter.apu;

import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.model.ApuId;
import com.budgetpro.domain.finanzas.apu.port.out.ApuRepository;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuInsumoEntity;
import com.budgetpro.infrastructure.persistence.mapper.apu.ApuMapper;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para ApuRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class ApuRepositoryAdapter implements ApuRepository {

    private final ApuJpaRepository jpaRepository;
    private final PartidaJpaRepository partidaJpaRepository;
    private final RecursoJpaRepository recursoJpaRepository;
    private final ApuMapper mapper;

    public ApuRepositoryAdapter(ApuJpaRepository jpaRepository,
                                PartidaJpaRepository partidaJpaRepository,
                                RecursoJpaRepository recursoJpaRepository,
                                ApuMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.partidaJpaRepository = partidaJpaRepository;
        this.recursoJpaRepository = recursoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(APU apu) {
        Optional<ApuEntity> existingEntityOpt = jpaRepository.findById(apu.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar
            ApuEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, apu);
            
            // Actualizar insumos (cascade y orphanRemoval manejan la sincronización)
            sincronizarInsumos(existingEntity, apu);
            
            jpaRepository.save(existingEntity);
        } else {
            // Creación: cargar entidades relacionadas y mapear
            PartidaEntity partidaEntity = partidaJpaRepository.findById(apu.getPartidaId())
                    .orElseThrow(() -> new IllegalStateException("Partida no encontrada: " + apu.getPartidaId()));

            ApuEntity newEntity = mapper.toEntity(apu, partidaEntity);
            
            // Cargar y asignar recursos a los insumos
            asignarRecursosAInsumos(newEntity, apu);
            
            jpaRepository.save(newEntity);
        }
    }

    /**
     * Asigna los recursos a los insumos de la entidad.
     */
    private void asignarRecursosAInsumos(ApuEntity entity, APU apu) {
        List<ApuInsumoEntity> insumosEntities = entity.getInsumos();
        List<com.budgetpro.domain.finanzas.apu.model.ApuInsumo> insumosDomain = apu.getInsumos();

        for (int i = 0; i < insumosEntities.size() && i < insumosDomain.size(); i++) {
            ApuInsumoEntity insumoEntity = insumosEntities.get(i);
            com.budgetpro.domain.finanzas.apu.model.ApuInsumo insumoDomain = insumosDomain.get(i);

            RecursoEntity recursoEntity = recursoJpaRepository.findById(insumoDomain.getRecursoId())
                    .orElseThrow(() -> new IllegalStateException("Recurso no encontrado: " + insumoDomain.getRecursoId()));

            insumoEntity.setRecurso(recursoEntity);
        }
    }

    /**
     * Sincroniza los insumos del dominio con los de la entidad.
     */
    private void sincronizarInsumos(ApuEntity existingEntity, APU apu) {
        // Limpiar insumos existentes y agregar los nuevos
        existingEntity.getInsumos().clear();
        
        for (com.budgetpro.domain.finanzas.apu.model.ApuInsumo insumoDomain : apu.getInsumos()) {
            RecursoEntity recursoEntity = recursoJpaRepository.findById(insumoDomain.getRecursoId())
                    .orElseThrow(() -> new IllegalStateException("Recurso no encontrado: " + insumoDomain.getRecursoId()));

            ApuInsumoEntity insumoEntity = mapper.toInsumoEntity(insumoDomain, existingEntity, recursoEntity);
            existingEntity.getInsumos().add(insumoEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<APU> findById(ApuId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<APU> findByPartidaId(UUID partidaId) {
        return jpaRepository.findByPartidaId(partidaId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPartidaId(UUID partidaId) {
        return jpaRepository.existsByPartidaId(partidaId);
    }
}
