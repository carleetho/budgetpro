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
 * Mapper para convertir entre Estimacion (dominio) y EstimacionEntity (persistencia).
 */
@Component
public class EstimacionMapper {

    private final DetalleEstimacionMapper detalleMapper;

    public EstimacionMapper(DetalleEstimacionMapper detalleMapper) {
        this.detalleMapper = detalleMapper;
    }

    /**
     * Convierte un Estimacion (dominio) a EstimacionEntity (persistencia) para CREACIÓN.
     * 
     * CRÍTICO: Para nuevas entidades, pasa null en version.
     * Hibernate inicializará la versión automáticamente.
     */
    public EstimacionEntity toEntity(Estimacion estimacion) {
        if (estimacion == null) {
            return null;
        }

        EstimacionEntity entity = new EstimacionEntity(
            estimacion.getId().getValue(),
            estimacion.getProyectoId(),
            estimacion.getNumeroEstimacion(),
            estimacion.getFechaCorte(),
            estimacion.getPeriodoInicio(),
            estimacion.getPeriodoFin(),
            estimacion.getMontoBruto(),
            estimacion.getAmortizacionAnticipo(),
            estimacion.getRetencionFondoGarantia(),
            estimacion.getMontoNetoPagar(),
            estimacion.getEvidenciaUrl(),
            estimacion.getEstado(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );

        // Mapear detalles
        if (estimacion.getDetalles() != null && !estimacion.getDetalles().isEmpty()) {
            List<DetalleEstimacionEntity> detallesEntity = estimacion.getDetalles().stream()
                    .map(detalle -> detalleMapper.toEntity(detalle, entity))
                    .collect(Collectors.toList());
            entity.setDetalles(detallesEntity);
        }

        return entity;
    }

    /**
     * Convierte un EstimacionEntity (persistencia) a Estimacion (dominio).
     */
    public Estimacion toDomain(EstimacionEntity entity) {
        if (entity == null) {
            return null;
        }

        // Mapear detalles
        List<DetalleEstimacion> detalles = entity.getDetalles().stream()
                .map(detalleMapper::toDomain)
                .collect(Collectors.toList());

        return Estimacion.reconstruir(
            EstimacionId.of(entity.getId()),
            entity.getProyectoId(),
            entity.getNumeroEstimacion(),
            entity.getFechaCorte(),
            entity.getPeriodoInicio(),
            entity.getPeriodoFin(),
            entity.getMontoBruto(),
            entity.getAmortizacionAnticipo(),
            entity.getRetencionFondoGarantia(),
            entity.getMontoNetoPagar(),
            entity.getEvidenciaUrl(),
            entity.getEstado(),
            detalles,
            entity.getVersion() != null ? entity.getVersion().longValue() : 0L
        );
    }

    /**
     * Actualiza una entidad existente con los datos del dominio.
     * 
     * CRÍTICO: NO se modifica la versión manualmente. Hibernate la incrementa automáticamente.
     */
    public void updateEntity(EstimacionEntity existingEntity, Estimacion estimacion) {
        existingEntity.setFechaCorte(estimacion.getFechaCorte());
        existingEntity.setPeriodoInicio(estimacion.getPeriodoInicio());
        existingEntity.setPeriodoFin(estimacion.getPeriodoFin());
        existingEntity.setMontoBruto(estimacion.getMontoBruto());
        existingEntity.setAmortizacionAnticipo(estimacion.getAmortizacionAnticipo());
        existingEntity.setRetencionFondoGarantia(estimacion.getRetencionFondoGarantia());
        existingEntity.setMontoNetoPagar(estimacion.getMontoNetoPagar());
        existingEntity.setEvidenciaUrl(estimacion.getEvidenciaUrl());
        existingEntity.setEstado(estimacion.getEstado());
        
        // Actualizar detalles: eliminar todos y agregar los nuevos
        existingEntity.getDetalles().clear();
        if (estimacion.getDetalles() != null && !estimacion.getDetalles().isEmpty()) {
            List<DetalleEstimacionEntity> nuevosDetalles = estimacion.getDetalles().stream()
                    .map(detalle -> detalleMapper.toEntity(detalle, existingEntity))
                    .collect(Collectors.toList());
            existingEntity.getDetalles().addAll(nuevosDetalles);
        }
        
        // CRÍTICO: NO se toca version. Hibernate lo maneja con @Version
        // CRÍTICO: NO se toca proyectoId ni numeroEstimacion (son inmutables después de crear)
    }
}
