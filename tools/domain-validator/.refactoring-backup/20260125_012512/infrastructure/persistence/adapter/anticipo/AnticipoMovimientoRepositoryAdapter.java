package com.budgetpro.infrastructure.persistence.adapter.anticipo;

import com.budgetpro.domain.finanzas.anticipo.model.AnticipoMovimiento;
import com.budgetpro.domain.finanzas.anticipo.port.out.AnticipoMovimientoRepository;
import com.budgetpro.infrastructure.persistence.entity.anticipo.AnticipoMovimientoEntity;
import com.budgetpro.infrastructure.persistence.mapper.anticipo.AnticipoMovimientoMapper;
import com.budgetpro.infrastructure.persistence.repository.anticipo.AnticipoMovimientoJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class AnticipoMovimientoRepositoryAdapter implements AnticipoMovimientoRepository {

    private final AnticipoMovimientoJpaRepository jpaRepository;
    private final AnticipoMovimientoMapper mapper;

    public AnticipoMovimientoRepositoryAdapter(AnticipoMovimientoJpaRepository jpaRepository,
                                               AnticipoMovimientoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public BigDecimal obtenerSaldoPendiente(UUID proyectoId) {
        return jpaRepository.obtenerSaldoPendiente(proyectoId);
    }

    @Override
    public void registrar(AnticipoMovimiento movimiento) {
        AnticipoMovimientoEntity entity = java.util.Objects.requireNonNull(
                mapper.toEntity(movimiento), "El movimiento no puede ser nulo");
        jpaRepository.save(entity);
    }
}
