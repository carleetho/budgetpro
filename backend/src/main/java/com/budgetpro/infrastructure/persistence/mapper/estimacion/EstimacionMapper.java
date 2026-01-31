package com.budgetpro.infrastructure.persistence.mapper.estimacion;

import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.infrastructure.persistence.entity.estimacion.DetalleEstimacionEntity;
import com.budgetpro.infrastructure.persistence.entity.estimacion.EstimacionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Estimacion (dominio) y EstimacionEntity
 * (persistencia).
 */
@Component
public class EstimacionMapper {

    /**
     * Convierte una Estimacion (dominio) a EstimacionEntity (persistencia).
     */
    public EstimacionEntity toEntity(Estimacion estimacion) {
        if (estimacion == null) {
            throw new IllegalArgumentException("Estimacion cannot be null for mapping");
        }

        EstimacionEntity entity = new EstimacionEntity(estimacion.getId().getValue(), estimacion.getProyectoId(),
                estimacion.getNumeroEstimacion(), estimacion.getFechaCorte(), estimacion.getPeriodoInicio(),
                estimacion.getPeriodoFin(), estimacion.getMontoBruto(), estimacion.getAmortizacionAnticipo(),
                estimacion.getRetencionFondoGarantia(), estimacion.getMontoNetoPagar(), estimacion.getEvidenciaUrl(),
                estimacion.getEstado(), null // Version manejada por Hibernate (null para nuevos)
        );

        // Mapear detalles
        if (estimacion.getDetalles() != null) {
            List<DetalleEstimacionEntity> detalleEntities = estimacion.getDetalles().stream()
                    .map(detalle -> toDetalleEntity(detalle, entity)).collect(Collectors.toList());
            entity.setDetalles(detalleEntities);
        }

        return entity;
    }

    /**
     * Mapea un DetalleEstimacion a su entidad.
     */
    private DetalleEstimacionEntity toDetalleEntity(DetalleEstimacion detalle, EstimacionEntity estimacionEntity) {
        if (detalle == null) {
            throw new IllegalArgumentException("Detalle cannot be null for mapping");
        }
        return new DetalleEstimacionEntity(detalle.getId().getValue(), estimacionEntity, detalle.getPartidaId(),
                detalle.getCantidadAvance(), detalle.getPrecioUnitario(), detalle.getImporte(),
                detalle.getAcumuladoAnterior(), null // Version manejada por Hibernate
        );
    }

    /**
     * Convierte una EstimacionEntity (persistencia) a Estimacion (dominio).
     */
    public Estimacion toDomain(EstimacionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("EstimacionEntity cannot be null for mapping");
        }

        List<DetalleEstimacion> detalles = new ArrayList<>();
        if (entity.getDetalles() != null) {
            detalles = entity.getDetalles().stream().map(this::toDetalleDomain).collect(Collectors.toList());
        }

        return Estimacion.reconstruir(EstimacionId.of(entity.getId()), entity.getProyectoId(),
                entity.getNumeroEstimacion(), entity.getFechaCorte(), entity.getPeriodoInicio(), entity.getPeriodoFin(),
                entity.getMontoBruto(), entity.getAmortizacionAnticipo(), entity.getRetencionFondoGarantia(),
                entity.getMontoNetoPagar(), entity.getEvidenciaUrl(), entity.getEstado(), detalles,
                entity.getVersion() != null ? entity.getVersion().longValue() : 0L);
    }

    /**
     * Mapea una entidad de detalle al dominio.
     */
    private DetalleEstimacion toDetalleDomain(DetalleEstimacionEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("DetalleEstimacionEntity cannot be null for mapping");
        }
        return DetalleEstimacion.reconstruir(DetalleEstimacionId.of(entity.getId()), entity.getPartidaId(),
                entity.getCantidadAvance(), entity.getPrecioUnitario(), entity.getImporte(),
                entity.getAcumuladoAnterior());
    }
}
