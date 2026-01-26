package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuInsumoSnapshotEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ApuSnapshotEntity;
import com.budgetpro.infrastructure.persistence.entity.catalogo.ComposicionCuadrillaSnapshotEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre APUInsumoSnapshot (dominio) y
 * ApuInsumoSnapshotEntity (persistencia).
 */
@Component
public class ApuInsumoSnapshotMapper {

    private final ComposicionCuadrillaSnapshotMapper composicionCuadrillaMapper;

    public ApuInsumoSnapshotMapper(ComposicionCuadrillaSnapshotMapper composicionCuadrillaMapper) {
        this.composicionCuadrillaMapper = composicionCuadrillaMapper;
    }

    public ApuInsumoSnapshotEntity toEntity(APUInsumoSnapshot insumo, ApuSnapshotEntity apuSnapshotEntity,
            UUID createdBy) {
        if (insumo == null) {
            return null;
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("El createdBy no puede ser nulo");
        }

        ApuInsumoSnapshotEntity entity = new ApuInsumoSnapshotEntity();
        entity.setId(insumo.getId().getValue());
        entity.setApuSnapshot(apuSnapshotEntity);
        entity.setRecursoExternalId(insumo.getRecursoExternalId());
        entity.setRecursoNombre(insumo.getRecursoNombre());
        entity.setCantidad(insumo.getCantidad());
        entity.setPrecioUnitario(insumo.getPrecioUnitario());
        entity.setSubtotal(insumo.getSubtotal());

        // Campos de clasificación
        entity.setTipoRecurso(insumo.getTipoRecurso());
        entity.setOrdenCalculo(insumo.getOrdenCalculo());

        // Campos de unidades
        entity.setAporteUnitario(insumo.getAporteUnitario());
        entity.setUnidadAporte(insumo.getUnidadAporte());
        entity.setUnidadBase(insumo.getUnidadBase());
        entity.setFactorConversionUnidadBase(insumo.getFactorConversionUnidadBase());
        entity.setUnidadCompra(insumo.getUnidadCompra());

        // Campos de precio/moneda
        entity.setMoneda(insumo.getMoneda());
        entity.setTipoCambioSnapshot(insumo.getTipoCambioSnapshot());
        entity.setPrecioMercado(insumo.getPrecioMercado());
        entity.setFlete(insumo.getFlete());
        entity.setPrecioPuestoEnObra(insumo.getPrecioPuestoEnObra());

        // Campos específicos MATERIAL
        entity.setDesperdicio(insumo.getDesperdicio());

        // Campos específicos MANO_OBRA
        entity.setCostoDiaCuadrillaCalculado(insumo.getCostoDiaCuadrillaCalculado());
        entity.setJornadaHoras(insumo.getJornadaHoras());

        // Campos específicos EQUIPO_MAQUINA
        entity.setCostoHoraMaquina(insumo.getCostoHoraMaquina());
        entity.setHorasUso(insumo.getHorasUso());

        // Campos específicos EQUIPO_HERRAMIENTA
        entity.setPorcentajeManoObra(insumo.getPorcentajeManoObra());
        entity.setDependeDe(insumo.getDependeDe());

        // Mapear composición de cuadrilla
        if (insumo.getComposicionCuadrilla() != null && !insumo.getComposicionCuadrilla().isEmpty()) {
            List<ComposicionCuadrillaSnapshotEntity> composicionEntities = insumo.getComposicionCuadrilla().stream()
                    .map(comp -> composicionCuadrillaMapper.toEntity(comp, entity, UUID.randomUUID(), createdBy))
                    .collect(Collectors.toList());
            entity.setComposicionCuadrilla(composicionEntities);
        }

        entity.setCreatedBy(createdBy);
        return entity;
    }

    public APUInsumoSnapshot toDomain(ApuInsumoSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }

        return APUInsumoSnapshot.reconstruir(APUInsumoSnapshotId.of(entity.getId()), entity.getRecursoExternalId(),
                entity.getRecursoNombre(), entity.getCantidad(), entity.getPrecioUnitario(), entity.getSubtotal(),
                // Campos de clasificación
                entity.getTipoRecurso(), entity.getOrdenCalculo(),
                // Campos de unidades
                entity.getAporteUnitario(), entity.getUnidadAporte(), entity.getUnidadBase(),
                entity.getFactorConversionUnidadBase(), entity.getUnidadCompra(),
                // Campos de precio/moneda
                entity.getMoneda(), entity.getTipoCambioSnapshot(), entity.getPrecioMercado(), entity.getFlete(),
                entity.getPrecioPuestoEnObra(),
                // Campos específicos MATERIAL
                entity.getDesperdicio(),
                // Campos específicos MANO_OBRA
                composicionCuadrillaMapper.toDomainList(entity.getComposicionCuadrilla()),
                entity.getCostoDiaCuadrillaCalculado(), entity.getJornadaHoras(),
                // Campos específicos EQUIPO_MAQUINA
                entity.getCostoHoraMaquina(), entity.getHorasUso(),
                // Campos específicos EQUIPO_HERRAMIENTA
                entity.getPorcentajeManoObra(), entity.getDependeDe());
    }
}
