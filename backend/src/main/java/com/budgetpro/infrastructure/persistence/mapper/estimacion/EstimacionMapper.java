package com.budgetpro.infrastructure.persistence.mapper.estimacion;

import com.budgetpro.domain.finanzas.estimacion.model.*;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionItemEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Estimacion (dominio) y EstimacionEntity
 * (persistencia).
 * 
 * CRÍTICO: Este mapper está alineado con la estructura actual del dominio y la
 * entidad JPA. Solo usa métodos públicos disponibles en el dominio.
 */
@Component
public class EstimacionMapper {

    /**
     * Convierte un Estimacion (dominio) a EstimacionEntity (persistencia) para
     * CREACIÓN.
     */
    public EstimacionEntity toEntity(Estimacion estimacion) {
        if (estimacion == null) {
            throw new IllegalArgumentException("La estimación no puede ser nula para convertir a entidad");
        }

        EstimacionEntity entity = new EstimacionEntity();
        entity.setId(estimacion.getId().getValue());
        entity.setPresupuestoId(estimacion.getPresupuestoId().getValue());
        entity.setNumeroEstimacion(0L); // TODO: Implementar generación de número de estimación
        entity.setEstado(estimacion.getEstado());
        entity.setFechaInicio(estimacion.getPeriodo().getFechaInicio());
        entity.setFechaFin(estimacion.getPeriodo().getFechaFin());
        entity.setRetencionPorcentaje(estimacion.getRetencionPorcentaje().getValue());
        // fechaCreacion, fechaAprobacion, aprobadoPor son manejados por Hibernate o no
        // son accesibles

        // Mapear items
        if (estimacion.getItems() != null && !estimacion.getItems().isEmpty()) {
            List<EstimacionItemEntity> itemsEntity = estimacion.getItems().stream()
                    .map(item -> toItemEntity(item, entity)).collect(Collectors.toList());
            entity.setItems(itemsEntity);
        }

        return entity;
    }

    /**
     * Convierte un EstimacionEntity (persistencia) a Estimacion (dominio).
     */
    public Estimacion toDomain(EstimacionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad EstimacionEntity no puede ser nula para convertir a dominio");
        }

        // Mapear items
        List<EstimacionItem> items = entity.getItems() != null
                ? entity.getItems().stream().map(this::toItemDomain).collect(Collectors.toList())
                : Collections.emptyList();

        return Estimacion.reconstruir(EstimacionId.of(entity.getId()), PresupuestoId.from(entity.getPresupuestoId()),
                PeriodoEstimacion.of(entity.getFechaInicio(), entity.getFechaFin()), entity.getEstado(),
                RetencionPorcentaje.of(entity.getRetencionPorcentaje()), items, null, // snapshot - TODO: Implementar si
                                                                                      // es necesario
                entity.getFechaCreacion(), entity.getFechaAprobacion(), entity.getAprobadoPor());
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     */
    public void updateEntity(EstimacionEntity existingEntity, Estimacion estimacion) {
        existingEntity.setEstado(estimacion.getEstado());
        existingEntity.setFechaInicio(estimacion.getPeriodo().getFechaInicio());
        existingEntity.setFechaFin(estimacion.getPeriodo().getFechaFin());
        existingEntity.setRetencionPorcentaje(estimacion.getRetencionPorcentaje().getValue());

        // Actualizar items: eliminar todos y agregar los nuevos
        existingEntity.getItems().clear();
        if (estimacion.getItems() != null && !estimacion.getItems().isEmpty()) {
            List<EstimacionItemEntity> nuevosItems = estimacion.getItems().stream()
                    .map(item -> toItemEntity(item, existingEntity)).collect(Collectors.toList());
            existingEntity.getItems().addAll(nuevosItems);
        }
    }

    /**
     * Convierte un EstimacionItem (dominio) a EstimacionItemEntity (persistencia).
     */
    private EstimacionItemEntity toItemEntity(EstimacionItem item, EstimacionEntity estimacion) {
        EstimacionItemEntity entity = new EstimacionItemEntity();
        entity.setId(item.getId().getValue());
        entity.setEstimacion(estimacion);
        entity.setPartidaId(item.getPartidaId());
        entity.setConcepto(item.getConcepto());
        entity.setMontoContractual(item.getMontoContractual().getValue());
        entity.setPorcentajeAnterior(item.getPorcentajeAnterior().getValue());
        entity.setMontoAnterior(item.getMontoAnterior().getValue());
        entity.setPorcentajeActual(item.getPorcentajeActual().getValue());
        entity.setMontoActual(item.getMontoActual().getValue());
        return entity;
    }

    /**
     * Convierte un EstimacionItemEntity (persistencia) a EstimacionItem (dominio).
     */
    private EstimacionItem toItemDomain(EstimacionItemEntity entity) {
        return EstimacionItem.reconstruir(EstimacionItemId.of(entity.getId()), entity.getPartidaId(),
                entity.getConcepto(), MontoEstimado.of(entity.getMontoContractual()),
                PorcentajeAvance.of(entity.getPorcentajeAnterior()), MontoEstimado.of(entity.getMontoAnterior()),
                PorcentajeAvance.of(entity.getPorcentajeActual()), MontoEstimado.of(entity.getMontoActual()));
    }
}
