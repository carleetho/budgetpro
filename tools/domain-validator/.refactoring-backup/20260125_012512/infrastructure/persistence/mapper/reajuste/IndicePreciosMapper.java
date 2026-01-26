package com.budgetpro.infrastructure.persistence.mapper.reajuste;

import com.budgetpro.domain.finanzas.reajuste.model.IndicePrecios;
import com.budgetpro.domain.finanzas.reajuste.model.IndicePreciosId;
import com.budgetpro.infrastructure.persistence.entity.reajuste.IndicePreciosEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre IndicePrecios (dominio) y IndicePreciosEntity (persistencia).
 */
@Component
public class IndicePreciosMapper {

    /**
     * Convierte un IndicePrecios (dominio) a IndicePreciosEntity (persistencia) para CREACIÓN.
     */
    public IndicePreciosEntity toEntity(IndicePrecios indice) {
        if (indice == null) {
            return null;
        }

        return new IndicePreciosEntity(
            indice.getId().getValue(),
            indice.getCodigo(),
            indice.getNombre(),
            indice.getTipo(),
            indice.getFechaBase(),
            indice.getValor(),
            indice.isActivo(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un IndicePreciosEntity (persistencia) a IndicePrecios (dominio).
     */
    public IndicePrecios toDomain(IndicePreciosEntity entity) {
        if (entity == null) {
            return null;
        }

        return IndicePrecios.reconstruir(
            IndicePreciosId.of(entity.getId()),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getTipo(),
            entity.getFechaBase(),
            entity.getValor(),
            entity.getActivo()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     */
    public void updateEntity(IndicePreciosEntity existingEntity, IndicePrecios indice) {
        existingEntity.setCodigo(indice.getCodigo());
        existingEntity.setNombre(indice.getNombre());
        existingEntity.setTipo(indice.getTipo());
        existingEntity.setFechaBase(indice.getFechaBase());
        existingEntity.setValor(indice.getValor());
        existingEntity.setActivo(indice.isActivo());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
    }
}
