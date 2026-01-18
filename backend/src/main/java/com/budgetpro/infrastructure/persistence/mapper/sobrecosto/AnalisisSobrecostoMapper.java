package com.budgetpro.infrastructure.persistence.mapper.sobrecosto;

import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecosto;
import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecostoId;
import com.budgetpro.infrastructure.persistence.entity.sobrecosto.AnalisisSobrecostoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre AnalisisSobrecosto (dominio) y AnalisisSobrecostoEntity (persistencia).
 */
@Component
public class AnalisisSobrecostoMapper {

    /**
     * Convierte un AnalisisSobrecosto (dominio) a AnalisisSobrecostoEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public AnalisisSobrecostoEntity toEntity(AnalisisSobrecosto analisis) {
        if (analisis == null) {
            return null;
        }

        return new AnalisisSobrecostoEntity(
            analisis.getId().getValue(),
            analisis.getPresupuestoId(),
            analisis.getPorcentajeIndirectosOficinaCentral(),
            analisis.getPorcentajeIndirectosOficinaCampo(),
            analisis.getPorcentajeFinanciamiento(),
            analisis.getFinanciamientoCalculado(),
            analisis.getPorcentajeUtilidad(),
            analisis.getPorcentajeFianzas(),
            analisis.getPorcentajeImpuestosReflejables(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un AnalisisSobrecostoEntity (persistencia) a AnalisisSobrecosto (dominio).
     */
    public AnalisisSobrecosto toDomain(AnalisisSobrecostoEntity entity) {
        if (entity == null) {
            return null;
        }

        return AnalisisSobrecosto.reconstruir(
            AnalisisSobrecostoId.of(entity.getId()),
            entity.getPresupuestoId(),
            entity.getPorcentajeIndirectosOficinaCentral(),
            entity.getPorcentajeIndirectosOficinaCampo(),
            entity.getPorcentajeFinanciamiento(),
            entity.getFinanciamientoCalculado(),
            entity.getPorcentajeUtilidad(),
            entity.getPorcentajeFianzas(),
            entity.getPorcentajeImpuestosReflejables(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(AnalisisSobrecostoEntity existingEntity, AnalisisSobrecosto analisis) {
        existingEntity.setPorcentajeIndirectosOficinaCentral(analisis.getPorcentajeIndirectosOficinaCentral());
        existingEntity.setPorcentajeIndirectosOficinaCampo(analisis.getPorcentajeIndirectosOficinaCampo());
        existingEntity.setPorcentajeFinanciamiento(analisis.getPorcentajeFinanciamiento());
        existingEntity.setFinanciamientoCalculado(analisis.getFinanciamientoCalculado());
        existingEntity.setPorcentajeUtilidad(analisis.getPorcentajeUtilidad());
        existingEntity.setPorcentajeFianzas(analisis.getPorcentajeFianzas());
        existingEntity.setPorcentajeImpuestosReflejables(analisis.getPorcentajeImpuestosReflejables());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca presupuestoId (es inmutable después de crear)
    }
}
