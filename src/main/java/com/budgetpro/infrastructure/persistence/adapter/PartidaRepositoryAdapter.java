package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.partida.PartidaId;
import com.budgetpro.domain.finanzas.port.out.PartidaRepository;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.exception.PartidaDuplicadaException;
import com.budgetpro.infrastructure.persistence.mapper.PartidaMapper;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia que implementa el puerto de salida PartidaRepository.
 * 
 * Implementa la persistencia de Partidas usando JPA/Hibernate.
 * Maneja optimistic locking, conversiones de dominio ↔ JPA y excepciones de integridad.
 */
@Component
public class PartidaRepositoryAdapter implements PartidaRepository {

    private final PartidaJpaRepository jpaRepository;
    private final PresupuestoJpaRepository presupuestoJpaRepository;
    private final PartidaMapper mapper;

    public PartidaRepositoryAdapter(PartidaJpaRepository jpaRepository,
                                   PresupuestoJpaRepository presupuestoJpaRepository,
                                   PartidaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.presupuestoJpaRepository = presupuestoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Partida save(Partida partida) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser nula");
        }

        try {
            // Buscar entidad Presupuesto (requerida para FK)
            UUID presupuestoId = partida.getPresupuestoId();
            PresupuestoEntity presupuestoEntity = presupuestoJpaRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalStateException(
                    String.format("No existe un presupuesto con ID %s", presupuestoId)));

            // Buscar entidad Partida existente (si existe)
            Optional<PartidaEntity> existingEntityOpt = jpaRepository.findById(partida.getId().getValue());

            PartidaEntity entity;
            if (existingEntityOpt.isPresent()) {
                // UPDATE: Actualizar entidad existente
                PartidaEntity existingEntity = existingEntityOpt.get();
                
                // Verificar optimistic locking: la versión debe coincidir
                if (!existingEntity.getVersion().equals(partida.getVersion())) {
                    throw new org.springframework.dao.OptimisticLockingFailureException(
                        String.format("Versión de optimistic locking no coincide. " +
                                     "Esperada: %s, Actual en BD: %s",
                                     partida.getVersion(), existingEntity.getVersion()));
                }
                
                entity = mapper.toEntity(partida, existingEntity, presupuestoEntity);
            } else {
                // CREATE: Crear nueva entidad
                entity = mapper.toEntity(partida, null, presupuestoEntity);
            }

            // Guardar la entidad (Hibernate incrementa version automáticamente con @Version)
            PartidaEntity savedEntity = jpaRepository.save(entity);

            // Reconstruir el agregado desde la entidad guardada para obtener la versión actualizada
            return mapper.toDomain(savedEntity);

        } catch (DataIntegrityViolationException e) {
            // Capturar violación de UNIQUE constraint (presupuesto_id, codigo)
            String message = e.getMessage() != null ? e.getMessage() : "";
            String causeMessage = e.getCause() != null && e.getCause().getMessage() != null 
                ? e.getCause().getMessage() : "";
            String fullMessage = message + " " + causeMessage;
            
            if (fullMessage.contains("uq_partida_presupuesto_codigo") ||
                fullMessage.contains("duplicate key value violates unique constraint") ||
                fullMessage.contains("UNIQUE constraint failed") ||
                (fullMessage.contains("partida") && fullMessage.contains("codigo") && fullMessage.contains("unique"))) {
                throw new PartidaDuplicadaException(
                    partida.getCodigo().getValue(),
                    partida.getPresupuestoId(),
                    e);
            }
            // Relanzar otras violaciones de integridad
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Partida> findById(PartidaId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la partida no puede ser nulo");
        }

        Optional<PartidaEntity> entityOpt = jpaRepository.findById(id.getValue());
        return entityOpt.map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findByProyectoId(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }

        List<PartidaEntity> entities = jpaRepository.findByProyectoId(proyectoId);
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Partida> findByPresupuestoId(UUID presupuestoId) {
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El presupuestoId no puede ser nulo");
        }

        List<PartidaEntity> entities = jpaRepository.findByPresupuestoId(presupuestoId);
        return entities.stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPresupuestoIdAndCodigo(UUID presupuestoId, String codigo) {
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El presupuestoId no puede ser nulo");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código no puede ser nulo ni vacío");
        }

        // Normalizar el código (igual que en CodigoPartida)
        String codigoNormalizado = codigo.trim().toUpperCase();
        return jpaRepository.existsByPresupuestoIdAndCodigo(presupuestoId, codigoNormalizado);
    }

    @Override
    @Transactional
    public void deleteById(PartidaId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la partida no puede ser nulo");
        }

        jpaRepository.deleteById(id.getValue());
    }
}
