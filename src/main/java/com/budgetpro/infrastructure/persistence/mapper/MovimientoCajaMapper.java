package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.model.TipoMovimiento;
import com.budgetpro.infrastructure.persistence.entity.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.MovimientoCajaEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mapper para convertir entre MovimientoCajaEntity (capa de infraestructura)
 * y MovimientoCaja (capa de dominio).
 */
@Component
public class MovimientoCajaMapper {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    /**
     * Convierte una MovimientoCajaEntity a un MovimientoCaja del dominio.
     * 
     * @param entity La entidad JPA
     * @return El objeto del dominio
     * @throws IllegalArgumentException si la entidad es nula
     */
    public MovimientoCaja toDomain(MovimientoCajaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad no puede ser nula");
        }

        // Convertir Value Objects desde primitivos
        BilleteraId billeteraId = BilleteraId.of(entity.getBilleteraId());
        TipoMovimiento tipo = TipoMovimiento.valueOf(entity.getTipo());
        
        // Normalizar monto
        BigDecimal monto = normalizarBigDecimal(entity.getMonto());

        // Reconstruir objeto del dominio usando factory method
        return MovimientoCaja.reconstruir(
            entity.getId(),
            billeteraId,
            monto,
            tipo,
            entity.getCreatedAt(),
            entity.getReferencia(),
            entity.getEvidenciaUrl()
        );
    }

    /**
     * Convierte un MovimientoCaja del dominio a una MovimientoCajaEntity.
     * 
     * @param movimiento El objeto del dominio
     * @param billeteraEntity La entidad Billetera asociada (requerida para FK)
     * @return La entidad JPA
     * @throws IllegalArgumentException si alguno de los parámetros requeridos es nulo
     */
    public MovimientoCajaEntity toEntity(MovimientoCaja movimiento, BilleteraEntity billeteraEntity) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo");
        }
        if (billeteraEntity == null) {
            throw new IllegalArgumentException("La entidad Billetera no puede ser nula (requerida para FK)");
        }

        // Crear nueva entidad
        MovimientoCajaEntity entity = new MovimientoCajaEntity(
            movimiento.getId(),
            billeteraEntity,
            movimiento.getTipo().name(), // Convertir enum a String
            movimiento.getMonto(),
            movimiento.getReferencia(),
            movimiento.getEvidenciaUrl()
        );

        // Establecer fecha si está presente en el dominio
        if (movimiento.getFecha() != null) {
            entity.setCreatedAt(movimiento.getFecha());
        }

        return entity;
    }

    /**
     * Normaliza un BigDecimal a escala 4 con redondeo HALF_EVEN (Banker's Rounding).
     * Garantiza que siempre se use la escala correcta para NUMERIC(19,4).
     */
    private static BigDecimal normalizarBigDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        return value.setScale(SCALE, ROUNDING_MODE);
    }
}
