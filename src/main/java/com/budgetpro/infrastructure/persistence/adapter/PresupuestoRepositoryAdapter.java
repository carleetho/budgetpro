package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.presupuesto.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.mapper.PresupuestoMapper;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de infraestructura que implementa el puerto de salida PresupuestoRepository.
 * 
 * Realiza el mapeo entre el agregado del dominio (Presupuesto) y la entidad de persistencia (PresupuestoEntity),
 * manejando las conversiones, optimistic locking y la persistencia de las Partida internas.
 * 
 * Sigue el patrón Adapter de Arquitectura Hexagonal.
 * 
 * NOTA: Las Partida son entidades internas del agregado Presupuesto y se persisten como parte del agregado raíz
 * mediante cascade ALL y orphanRemoval en la relación @OneToMany.
 */
@Component
public class PresupuestoRepositoryAdapter implements PresupuestoRepository {

    private final PresupuestoJpaRepository jpaRepository;
    private final PresupuestoMapper mapper;

    public PresupuestoRepositoryAdapter(PresupuestoJpaRepository jpaRepository,
                                       PresupuestoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void save(Presupuesto presupuesto) {
        if (presupuesto == null) {
            throw new IllegalArgumentException("El presupuesto no puede ser nulo");
        }

        // 1. Buscar entidad existente (si existe)
        Optional<PresupuestoEntity> existingEntityOpt = jpaRepository.findById(presupuesto.getId().getValue());

        // 2. Mapear dominio a entidad (Hibernate maneja optimistic locking automáticamente con @Version)
        PresupuestoEntity entityToSave = mapper.toEntity(presupuesto, existingEntityOpt.orElse(null));

        // 3. Guardar presupuesto (Hibernate persiste las partidas mediante cascade ALL y maneja optimistic locking automáticamente)
        jpaRepository.save(entityToSave);
    }

    @Override
    public Optional<Presupuesto> findById(PresupuestoId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del presupuesto no puede ser nulo");
        }

        // Buscar entidad con partidas cargadas (FetchType.LAZY, pero las cargamos explícitamente)
        return jpaRepository.findById(id.getValue())
                .map(entity -> {
                    // Forzar carga de partidas (si no están ya cargadas)
                    entity.getPartidas().size(); // Lazy load trigger
                    return mapper.toDomain(entity);
                });
    }

    @Override
    public Optional<Presupuesto> findByProyectoId(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }

        // Buscar presupuestos del proyecto (puede haber múltiples, retornamos el primero)
        return jpaRepository.findByProyectoId(proyectoId).stream()
                .findFirst()
                .map(entity -> {
                    // Forzar carga de partidas (si no están ya cargadas)
                    entity.getPartidas().size(); // Lazy load trigger
                    return mapper.toDomain(entity);
                });
    }

    @Override
    public boolean existsById(PresupuestoId id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del presupuesto no puede ser nulo");
        }

        return jpaRepository.existsById(id.getValue());
    }
}
