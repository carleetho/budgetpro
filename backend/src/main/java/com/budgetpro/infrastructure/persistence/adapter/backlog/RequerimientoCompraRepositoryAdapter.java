package com.budgetpro.infrastructure.persistence.adapter.backlog;

import com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento;
import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompra;
import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompraId;
import com.budgetpro.domain.logistica.backlog.port.out.RequerimientoCompraRepository;
import com.budgetpro.infrastructure.persistence.entity.backlog.RequerimientoCompraEntity;
import com.budgetpro.infrastructure.persistence.mapper.backlog.RequerimientoCompraMapper;
import com.budgetpro.infrastructure.persistence.repository.backlog.RequerimientoCompraJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para RequerimientoCompraRepository.
 */
@Component
public class RequerimientoCompraRepositoryAdapter implements RequerimientoCompraRepository {

    private final RequerimientoCompraJpaRepository jpaRepository;
    private final RequerimientoCompraMapper mapper;

    public RequerimientoCompraRepositoryAdapter(RequerimientoCompraJpaRepository jpaRepository,
                                                RequerimientoCompraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void save(RequerimientoCompra requerimiento) {
        Optional<RequerimientoCompraEntity> existingEntityOpt = jpaRepository.findById(requerimiento.getId().getValue());

        if (existingEntityOpt.isPresent()) {
            // Actualización: actualizar estado
            RequerimientoCompraEntity existingEntity = existingEntityOpt.get();
            mapper.updateEntity(existingEntity, requerimiento);
            jpaRepository.save(existingEntity);
        } else {
            // Creación: mapear y guardar
            RequerimientoCompraEntity newEntity = mapper.toEntity(requerimiento);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RequerimientoCompra> findById(RequerimientoCompraId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequerimientoCompra> findPendientesPorRecurso(UUID proyectoId, String recursoExternalId, String unidadMedida) {
        List<EstadoRequerimiento> estadosPendientes = List.of(
                EstadoRequerimiento.PENDIENTE,
                EstadoRequerimiento.EN_COTIZACION,
                EstadoRequerimiento.ORDENADA
        );
        return mapper.toDomainList(
                jpaRepository.findPendientesPorRecurso(proyectoId, recursoExternalId, unidadMedida, estadosPendientes)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequerimientoCompra> findByRequisicionId(UUID requisicionId) {
        return mapper.toDomainList(jpaRepository.findByRequisicionId(requisicionId));
    }
}
