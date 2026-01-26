package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.estimacion.model.*;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EstimacionMapper {

    public Estimacion toDomain(EstimacionEntity entity) {
        if (entity == null)
            return null;

        List<EstimacionItem> items = entity.getItems().stream().map(this::toDomainItem).collect(Collectors.toList());

        return Estimacion.reconstruir(EstimacionId.of(entity.getId()), PresupuestoId.of(entity.getPresupuestoId()),
                PeriodoEstimacion.reconstruir(entity.getFechaInicio(), entity.getFechaFin()), entity.getEstado(),
                RetencionPorcentaje.reconstruir(entity.getRetencionPorcentaje()), items);
    }

    public EstimacionItem toDomainItem(EstimacionItemEntity entity) {
        if (entity == null)
            return null;

        return EstimacionItem.reconstruir(EstimacionItemId.of(entity.getId()), entity.getPartidaId(),
                entity.getConcepto(), MontoEstimado.of(entity.getMontoContractual()),
                PorcentajeAvance.of(entity.getPorcentajeAnterior()), MontoEstimado.of(entity.getMontoAnterior()),
                PorcentajeAvance.of(entity.getPorcentajeActual()), MontoEstimado.of(entity.getMontoActual()));
    }

    public EstimacionEntity toEntity(Estimacion domain) {
        if (domain == null)
            return null;

        EstimacionEntity entity = new EstimacionEntity();
        entity.setId(domain.getId().getValue());
        entity.setPresupuestoId(domain.getPresupuestoId().getValue());
        entity.setEstado(domain.getEstado());
        entity.setFechaInicio(domain.getPeriodo().getFechaInicio());
        entity.setFechaFin(domain.getPeriodo().getFechaFin());
        entity.setRetencionPorcentaje(domain.getRetencionPorcentaje().getValue());

        // Items handled via specialized method or cascade, usually adapter handles
        // collection update logic
        // But for initial creation/full update:
        List<EstimacionItemEntity> itemEntities = domain.getItems().stream().map(item -> toEntityItem(item, entity))
                .collect(Collectors.toList());
        entity.setItems(itemEntities);

        return entity;
    }

    public EstimacionItemEntity toEntityItem(EstimacionItem domain, EstimacionEntity parent) {
        if (domain == null)
            return null;

        EstimacionItemEntity entity = new EstimacionItemEntity();
        entity.setId(domain.getId().getValue());
        entity.setEstimacion(parent);
        entity.setPartidaId(domain.getPartidaId());
        entity.setConcepto(domain.getConcepto());
        entity.setMontoContractual(domain.getMontoContractual().getValueForPersistence());
        entity.setPorcentajeAnterior(domain.getPorcentajeAnterior().getValue());
        entity.setMontoAnterior(domain.getMontoAnterior().getValueForPersistence());
        entity.setPorcentajeActual(domain.getPorcentajeActual().getValue());
        entity.setMontoActual(domain.getMontoActual().getValueForPersistence());

        return entity;
    }
}
