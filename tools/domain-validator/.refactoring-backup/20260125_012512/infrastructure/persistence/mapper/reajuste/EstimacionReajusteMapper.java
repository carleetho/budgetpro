package com.budgetpro.infrastructure.persistence.mapper.reajuste;

import com.budgetpro.domain.finanzas.reajuste.model.DetalleReajustePartida;
import com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajuste;
import com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajusteId;
import com.budgetpro.infrastructure.persistence.entity.reajuste.DetalleReajustePartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.reajuste.EstimacionReajusteEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre EstimacionReajuste (dominio) y EstimacionReajusteEntity (persistencia).
 */
@Component
public class EstimacionReajusteMapper {

    private final DetalleReajustePartidaMapper detalleMapper;

    public EstimacionReajusteMapper(DetalleReajustePartidaMapper detalleMapper) {
        this.detalleMapper = detalleMapper;
    }

    /**
     * Convierte un EstimacionReajuste (dominio) a EstimacionReajusteEntity (persistencia) para CREACIÓN.
     */
    public EstimacionReajusteEntity toEntity(EstimacionReajuste estimacion) {
        if (estimacion == null) {
            return null;
        }

        EstimacionReajusteEntity entity = new EstimacionReajusteEntity(
            estimacion.getId().getValue(),
            estimacion.getProyectoId(),
            estimacion.getPresupuestoId(),
            estimacion.getNumeroEstimacion(),
            estimacion.getFechaCorte(),
            estimacion.getIndiceBaseCodigo(),
            estimacion.getIndiceBaseFecha(),
            estimacion.getIndiceActualCodigo(),
            estimacion.getIndiceActualFecha(),
            estimacion.getValorIndiceBase(),
            estimacion.getValorIndiceActual(),
            estimacion.getMontoBase(),
            estimacion.getMontoReajustado(),
            estimacion.getDiferencial(),
            estimacion.getPorcentajeVariacion(),
            estimacion.getEstado(),
            estimacion.getObservaciones(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );

        // Mapear detalles
        if (estimacion.getDetalles() != null && !estimacion.getDetalles().isEmpty()) {
            List<DetalleReajustePartidaEntity> detallesEntity = estimacion.getDetalles().stream()
                    .map(detalle -> detalleMapper.toEntity(detalle, entity))
                    .collect(Collectors.toList());
            entity.setDetalles(detallesEntity);
        }

        return entity;
    }

    /**
     * Convierte un EstimacionReajusteEntity (persistencia) a EstimacionReajuste (dominio).
     */
    public EstimacionReajuste toDomain(EstimacionReajusteEntity entity) {
        if (entity == null) {
            return null;
        }

        // Mapear detalles
        List<DetalleReajustePartida> detalles = entity.getDetalles().stream()
                .map(detalleMapper::toDomain)
                .collect(Collectors.toList());

        return EstimacionReajuste.reconstruir(
            EstimacionReajusteId.of(entity.getId()),
            entity.getProyectoId(),
            entity.getPresupuestoId(),
            entity.getNumeroEstimacion(),
            entity.getFechaCorte(),
            entity.getIndiceBaseCodigo(),
            entity.getIndiceBaseFecha(),
            entity.getIndiceActualCodigo(),
            entity.getIndiceActualFecha(),
            entity.getValorIndiceBase(),
            entity.getValorIndiceActual(),
            entity.getMontoBase(),
            entity.getMontoReajustado(),
            entity.getDiferencial(),
            entity.getPorcentajeVariacion(),
            entity.getEstado(),
            entity.getObservaciones(),
            detalles,
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     */
    public void updateEntity(EstimacionReajusteEntity existingEntity, EstimacionReajuste estimacion) {
        existingEntity.setFechaCorte(estimacion.getFechaCorte());
        existingEntity.setMontoBase(estimacion.getMontoBase());
        existingEntity.setMontoReajustado(estimacion.getMontoReajustado());
        existingEntity.setDiferencial(estimacion.getDiferencial());
        existingEntity.setPorcentajeVariacion(estimacion.getPorcentajeVariacion());
        existingEntity.setEstado(estimacion.getEstado());
        existingEntity.setObservaciones(estimacion.getObservaciones());
        
        // Actualizar detalles: eliminar todos y agregar los nuevos
        existingEntity.getDetalles().clear();
        if (estimacion.getDetalles() != null && !estimacion.getDetalles().isEmpty()) {
            List<DetalleReajustePartidaEntity> nuevosDetalles = estimacion.getDetalles().stream()
                    .map(detalle -> detalleMapper.toEntity(detalle, existingEntity))
                    .collect(Collectors.toList());
            existingEntity.getDetalles().addAll(nuevosDetalles);
        }
        
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se tocan campos inmutables (proyectoId, presupuestoId, numeroEstimacion, índices)
    }
}
