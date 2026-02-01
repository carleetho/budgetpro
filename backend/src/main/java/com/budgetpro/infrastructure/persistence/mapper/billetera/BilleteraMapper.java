package com.budgetpro.infrastructure.persistence.mapper.billetera;

import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.infrastructure.persistence.entity.billetera.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.billetera.MovimientoCajaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Billetera (dominio) y BilleteraEntity
 * (persistencia).
 */
@Component
public class BilleteraMapper {

    /**
     * Convierte un Billetera (dominio) a BilleteraEntity (persistencia) para
     * CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version. Hibernate inicializará
     * la versión automáticamente.
     */
    public BilleteraEntity toEntity(Billetera billetera) {
        if (billetera == null) {
            throw new IllegalArgumentException("La billetera no puede ser nula");
        }

        BilleteraEntity entity = new BilleteraEntity(billetera.getId().getValue(), billetera.getProyectoId(),
                billetera.getMoneda(), billetera.getSaldoActual(), null // CRÍTICO: null para nuevas entidades,
                                                                        // Hibernate manejará la versión
        );

        // Mapear movimientos nuevos
        List<MovimientoCajaEntity> movimientosEntities = billetera.getMovimientosNuevos().stream()
                .map(movimiento -> toMovimientoEntity(movimiento, entity)).collect(Collectors.toList());
        entity.setMovimientos(movimientosEntities);

        return entity;
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa
     * automáticamente.
     */
    public void updateEntity(BilleteraEntity existingEntity, Billetera billetera) {
        existingEntity.setSaldoActual(billetera.getSaldoActual());
        existingEntity.setMoneda(billetera.getMoneda());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version

        // Agregar movimientos nuevos
        List<MovimientoCajaEntity> movimientosNuevos = billetera.getMovimientosNuevos().stream()
                .map(movimiento -> toMovimientoEntity(movimiento, existingEntity)).collect(Collectors.toList());
        existingEntity.getMovimientos().addAll(movimientosNuevos);
    }

    /**
     * Convierte un MovimientoCaja (dominio) a MovimientoCajaEntity (persistencia).
     */
    private MovimientoCajaEntity toMovimientoEntity(MovimientoCaja movimiento, BilleteraEntity billeteraEntity) {
        return new MovimientoCajaEntity(movimiento.getId(), billeteraEntity, movimiento.getMonto(),
                movimiento.getMoneda(), movimiento.getTipo(), movimiento.getFecha(), movimiento.getReferencia(),
                movimiento.getEvidenciaUrl(), movimiento.getEstado());
    }

    /**
     * Convierte un BilleteraEntity (persistencia) a Billetera (dominio).
     */
    public Billetera toDomain(BilleteraEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad billetera no puede ser nula");
        }

        return Billetera.reconstruir(BilleteraId.of(entity.getId()), entity.getProyectoId(),
                entity.getMoneda() != null ? entity.getMoneda() : "PEN", entity.getSaldoActual(),
                entity.getVersion() != null ? entity.getVersion().longValue() : 0L);
    }

    /**
     * Convierte un MovimientoCajaEntity (persistencia) a MovimientoCaja (dominio).
     */
    public MovimientoCaja toMovimientoDomain(MovimientoCajaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad movimiento no puede ser nula");
        }

        return MovimientoCaja.reconstruir(entity.getId(), BilleteraId.of(entity.getBilletera().getId()),
                entity.getMonto(), entity.getMoneda() != null ? entity.getMoneda() : "PEN", entity.getTipo(),
                entity.getFecha(), entity.getReferencia(), entity.getEvidenciaUrl(), entity.getEstado());
    }
}
