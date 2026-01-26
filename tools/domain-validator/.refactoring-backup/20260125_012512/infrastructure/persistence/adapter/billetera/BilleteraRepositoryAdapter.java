package com.budgetpro.infrastructure.persistence.adapter.billetera;

import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.infrastructure.persistence.entity.billetera.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.mapper.billetera.BilleteraMapper;
import com.budgetpro.infrastructure.persistence.repository.billetera.BilleteraJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para BilleteraRepository.
 * 
 * CRÍTICO: NO se hacen validaciones manuales de versión.
 * Hibernate maneja el Optimistic Locking automáticamente con @Version.
 */
@Component
public class BilleteraRepositoryAdapter implements BilleteraRepository {

    private final BilleteraJpaRepository jpaRepository;
    private final BilleteraMapper mapper;

    public BilleteraRepositoryAdapter(BilleteraJpaRepository jpaRepository, BilleteraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Billetera> findByProyectoId(UUID proyectoId) {
        return jpaRepository.findByProyectoId(proyectoId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Billetera> findById(BilleteraId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void save(Billetera billetera) {
        Optional<BilleteraEntity> existingEntityOpt = jpaRepository.findById(billetera.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar campos y guardar movimientos nuevos
            BilleteraEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, billetera);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            BilleteraEntity newEntity = mapper.toEntity(billetera);
            jpaRepository.save(newEntity);
        }

        // Limpiar movimientos nuevos después de persistir exitosamente
        billetera.limpiarMovimientosNuevos();
    }
}
