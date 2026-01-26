package com.budgetpro.infrastructure.persistence.mapper.apu;

import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.model.ApuId;
import com.budgetpro.domain.finanzas.apu.model.ApuInsumo;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuInsumoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre APU (dominio) y ApuEntity (persistencia).
 */
@Component
public class ApuMapper {

    /**
     * Convierte un APU (dominio) a ApuEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public ApuEntity toEntity(APU apu, PartidaEntity partidaEntity) {
        if (apu == null) {
            return null;
        }

        ApuEntity entity = new ApuEntity(
            apu.getId().getValue(),
            partidaEntity,
            apu.getRendimiento(),
            apu.getUnidad(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );

        // Mapear insumos
        List<ApuInsumoEntity> insumosEntities = apu.getInsumos().stream()
                .map(insumo -> toInsumoEntity(insumo, entity, null)) // recursoEntity se carga después
                .collect(Collectors.toList());
        entity.setInsumos(insumosEntities);

        return entity;
    }

    /**
     * Convierte un ApuInsumo (dominio) a ApuInsumoEntity (persistencia).
     */
    public ApuInsumoEntity toInsumoEntity(ApuInsumo insumo, ApuEntity apuEntity, RecursoEntity recursoEntity) {
        if (insumo == null) {
            return null;
        }

        return new ApuInsumoEntity(
            insumo.getId().getValue(),
            apuEntity,
            recursoEntity, // Debe ser cargado antes de llamar este método
            insumo.getCantidad(),
            insumo.getPrecioUnitario(),
            insumo.getSubtotal(),
            null // CRÍTICO: null para nuevas entidades, Hibernate manejará la versión
        );
    }

    /**
     * Convierte un ApuEntity (persistencia) a APU (dominio).
     */
    public APU toDomain(ApuEntity entity) {
        if (entity == null) {
            return null;
        }

        UUID partidaId = entity.getPartida() != null ? entity.getPartida().getId() : null;

        // Mapear insumos
        List<ApuInsumo> insumos = entity.getInsumos().stream()
                .map(this::toInsumoDomain)
                .collect(Collectors.toList());

        return APU.reconstruir(
            ApuId.from(entity.getId()),
            partidaId,
            entity.getRendimiento(),
            entity.getUnidad(),
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L,
            insumos
        );
    }

    /**
     * Convierte un ApuInsumoEntity (persistencia) a ApuInsumo (dominio).
     */
    public ApuInsumo toInsumoDomain(ApuInsumoEntity entity) {
        if (entity == null) {
            return null;
        }

        UUID recursoId = entity.getRecurso() != null ? entity.getRecurso().getId() : null;

        return ApuInsumo.reconstruir(
            com.budgetpro.domain.finanzas.apu.model.ApuInsumoId.from(entity.getId()),
            recursoId,
            entity.getCantidad(),
            entity.getPrecioUnitario(),
            entity.getSubtotal()
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(ApuEntity existingEntity, APU apu) {
        existingEntity.setRendimiento(apu.getRendimiento());
        existingEntity.setUnidad(apu.getUnidad());
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca partida (es inmutable después de crear)
        // Los insumos se manejan con cascade y orphanRemoval
    }
}
