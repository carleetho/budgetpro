package com.budgetpro.infrastructure.persistence.mapper.consumo;

import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartidaId;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.consumo.ConsumoPartidaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper para convertir entre ConsumoPartida (dominio) y ConsumoPartidaEntity (persistencia).
 */
@Component
public class ConsumoPartidaMapper {

    /**
     * Convierte un ConsumoPartida (dominio) a ConsumoPartidaEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ConsumoPartidaEntity toEntity(ConsumoPartida consumo, PartidaEntity partidaEntity) {
        if (consumo == null) {
            return null;
        }

        return new ConsumoPartidaEntity(
            consumo.getId().getValue(),
            partidaEntity,
            consumo.getCompraDetalleId(),
            consumo.getMonto(),
            consumo.getFecha(),
            consumo.getTipo(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un ConsumoPartidaEntity (persistencia) a ConsumoPartida (dominio).
     */
    public ConsumoPartida toDomain(ConsumoPartidaEntity entity) {
        if (entity == null) {
            return null;
        }

        UUID partidaId = entity.getPartida() != null ? entity.getPartida().getId() : null;

        return ConsumoPartida.reconstruir(
            ConsumoPartidaId.from(entity.getId()),
            partidaId,
            entity.getCompraDetalleId(),
            entity.getMonto(),
            entity.getFecha(),
            entity.getTipo(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }
}
