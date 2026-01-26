package com.budgetpro.infrastructure.persistence.mapper.anticipo;

import com.budgetpro.domain.finanzas.anticipo.model.AnticipoMovimiento;
import com.budgetpro.infrastructure.persistence.entity.anticipo.AnticipoMovimientoEntity;
import org.springframework.stereotype.Component;

@Component
public class AnticipoMovimientoMapper {

    public AnticipoMovimientoEntity toEntity(AnticipoMovimiento movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo");
        }
        return new AnticipoMovimientoEntity(
                movimiento.getId(),
                movimiento.getProyectoId(),
                movimiento.getMonto(),
                movimiento.getTipo(),
                movimiento.getFecha(),
                movimiento.getReferencia()
        );
    }
}
