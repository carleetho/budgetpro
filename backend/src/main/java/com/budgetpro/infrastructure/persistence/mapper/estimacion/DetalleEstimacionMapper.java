package com.budgetpro.infrastructure.persistence.mapper.estimacion;

import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacionId;
import com.budgetpro.infrastructure.persistence.entity.estimacion.DetalleEstimacionEntity;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre DetalleEstimacion (dominio) y DetalleEstimacionEntity (persistencia).
 */
@Component
public class DetalleEstimacionMapper {

    /**
     * Convierte un DetalleEstimacion (dominio) a DetalleEstimacionEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public DetalleEstimacionEntity toEntity(DetalleEstimacion detalle, EstimacionEntity estimacionEntity) {
        if (detalle == null) {
            return null;
        }

        return new DetalleEstimacionEntity(
            detalle.getId().getValue(),
            estimacionEntity,
            detalle.getPartidaId(),
            detalle.getCantidadAvance(),
            detalle.getPrecioUnitario(),
            detalle.getImporte(),
            detalle.getAcumuladoAnterior(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un DetalleEstimacionEntity (persistencia) a DetalleEstimacion (dominio).
     */
    public DetalleEstimacion toDomain(DetalleEstimacionEntity entity) {
        if (entity == null) {
            return null;
        }

        return DetalleEstimacion.reconstruir(
            DetalleEstimacionId.of(entity.getId()),
            entity.getPartidaId(),
            entity.getCantidadAvance(),
            entity.getPrecioUnitario(),
            entity.getImporte(),
            entity.getAcumuladoAnterior()
        );
    }
}
