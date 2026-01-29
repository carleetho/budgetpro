package com.budgetpro.infrastructure.persistence.mapper.estimacion;

import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.infrastructure.persistence.entity.estimacion.DetalleEstimacionEntity;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Estimacion (dominio) y EstimacionEntity
 * (persistencia).
 */
@Component
public class EstimacionMapper {

    private final DetalleEstimacionMapper detalleMapper;

    public EstimacionMapper(DetalleEstimacionMapper detalleMapper) {
        this.detalleMapper = detalleMapper;
    }

    /**
     * Convierte un Estimacion (dominio) a EstimacionEntity (persistencia) para
     * CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version. Hibernate inicializará
     * la versión automáticamente.
     */
    public EstimacionEntity toEntity(Estimacion estimacion) {
        if (estimacion == null) {
            return null;
        }

        EstimacionEntity entity = new EstimacionEntity(estimacion.getId().getValue(), estimacion.getProyectoId(), // Use
                                                                                                                  // proyectoId
                                                                                                                  // directly
                                                                                                                  // (it's
                                                                                                                  // UUID
                                                                                                                  // now)
                estimacion.getNumeroEstimacion().longValue(), estimacion.getFechaCorte(), estimacion.getPeriodoInicio(),
                estimacion.getPeriodoFin(), estimacion.getMontoBruto(), estimacion.getAmortizacionAnticipo(),
                estimacion.getRetencionFondoGarantia(), estimacion.getMontoNetoPagar(), estimacion.getEvidenciaUrl(),
                estimacion.getEstado(), null // version
        );

        entity.setRetencionPorcentaje(estimacion.getRetencionPorcentaje().getValue());

        if (estimacion.getDetalles() != null && !estimacion.getDetalles().isEmpty()) {
            List<DetalleEstimacionEntity> detallesEntity = estimacion.getDetalles().stream()
                    .map(detalle -> detalleMapper.toEntity(detalle, entity)).collect(Collectors.toList());
            entity.setDetalles(detallesEntity);
        }

        return entity;
    }

    public Estimacion toDomain(EstimacionEntity entity) {
        if (entity == null) {
            return null;
        }

        List<DetalleEstimacion> detalles = entity.getDetalles().stream().map(detalleMapper::toDomain)
                .collect(Collectors.toList());

        return Estimacion.reconstruir(EstimacionId.of(entity.getId()), entity.getPresupuestoId(), // Using BudgetID as
                                                                                                  // ProjectID proxy or
                                                                                                  // assuming mapping
                entity.getNumeroEstimacion().intValue(), entity.getFechaCorte(), entity.getFechaInicio(),
                entity.getFechaFin(), entity.getMontoBruto(), entity.getAmortizacionAnticipo(),
                entity.getRetencionFondoGarantia(), entity.getMontoNetoPagar(), entity.getEvidenciaUrl(),
                entity.getEstado(), detalles, entity.getVersion() != null ? entity.getVersion().longValue() : 0L);
    }

    public void updateEntity(EstimacionEntity existingEntity, Estimacion estimacion) {
        existingEntity.setFechaCorte(estimacion.getFechaCorte());
        existingEntity.setFechaInicio(estimacion.getPeriodoInicio());
        existingEntity.setFechaFin(estimacion.getPeriodoFin());
        existingEntity.setMontoBruto(estimacion.getMontoBruto());
        existingEntity.setAmortizacionAnticipo(estimacion.getAmortizacionAnticipo());
        existingEntity.setRetencionFondoGarantia(estimacion.getRetencionFondoGarantia());
        existingEntity.setMontoNetoPagar(estimacion.getMontoNetoPagar());
        existingEntity.setEvidenciaUrl(estimacion.getEvidenciaUrl());
        existingEntity.setEstado(estimacion.getEstado());
        existingEntity.setRetencionPorcentaje(estimacion.getRetencionPorcentaje().getValue());

        existingEntity.getDetalles().clear();
        if (estimacion.getDetalles() != null && !estimacion.getDetalles().isEmpty()) {
            List<DetalleEstimacionEntity> nuevosDetalles = estimacion.getDetalles().stream()
                    .map(detalle -> detalleMapper.toEntity(detalle, existingEntity)).collect(Collectors.toList());
            existingEntity.getDetalles().addAll(nuevosDetalles);
        }
    }
}
