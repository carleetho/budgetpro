package com.budgetpro.infrastructure.persistence.mapper.reajuste;

import com.budgetpro.domain.finanzas.reajuste.model.DetalleReajustePartida;
import com.budgetpro.domain.finanzas.reajuste.model.DetalleReajustePartidaId;
import com.budgetpro.infrastructure.persistence.entity.reajuste.DetalleReajustePartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.reajuste.EstimacionReajusteEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre DetalleReajustePartida (dominio) y DetalleReajustePartidaEntity (persistencia).
 */
@Component
public class DetalleReajustePartidaMapper {

    /**
     * Convierte un DetalleReajustePartida (dominio) a DetalleReajustePartidaEntity (persistencia) para CREACIÓN.
     */
    public DetalleReajustePartidaEntity toEntity(DetalleReajustePartida detalle, EstimacionReajusteEntity estimacionEntity) {
        if (detalle == null) {
            return null;
        }

        return new DetalleReajustePartidaEntity(
            detalle.getId().getValue(),
            estimacionEntity,
            detalle.getPartidaId(),
            detalle.getMontoBase(),
            detalle.getMontoReajustado(),
            detalle.getDiferencial(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un DetalleReajustePartidaEntity (persistencia) a DetalleReajustePartida (dominio).
     */
    public DetalleReajustePartida toDomain(DetalleReajustePartidaEntity entity) {
        if (entity == null) {
            return null;
        }

        return DetalleReajustePartida.reconstruir(
            DetalleReajustePartidaId.of(entity.getId()),
            entity.getPartidaId(),
            entity.getMontoBase(),
            entity.getMontoReajustado(),
            entity.getDiferencial()
        );
    }
}
