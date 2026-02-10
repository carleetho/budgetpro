package com.budgetpro.application.produccion.service;

import com.budgetpro.application.produccion.exception.BusinessRuleException;
import com.budgetpro.application.produccion.validation.ProduccionValidator;
import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.EstadoReporteProduccion;
import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import com.budgetpro.infrastructure.persistence.repository.produccion.ReporteProduccionJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de Producción (RPC).
 */
@Service
@SuppressWarnings("null")
public class ProduccionServiceImpl implements ProduccionService {

    private final ReporteProduccionJpaRepository reporteProduccionJpaRepository;
    private final ProduccionValidator produccionValidator;

    public ProduccionServiceImpl(ReporteProduccionJpaRepository reporteProduccionJpaRepository,
                                 ProduccionValidator produccionValidator) {
        this.reporteProduccionJpaRepository = reporteProduccionJpaRepository;
        this.produccionValidator = produccionValidator;
    }

    @Override
    @Transactional
    public ReporteProduccionEntity crearReporte(ReporteProduccionEntity reporte) {
        if (reporte == null) {
            // REGLA-009
            throw new BusinessRuleException("El reporte no puede ser nulo.");
        }

        produccionValidator.validarFechaNoFutura(reporte.getFechaReporte());
        // REGLA-008
        if (reporte.getEstado() == null) {
            reporte.setEstado(EstadoReporteProduccion.PENDIENTE);
        }

        validarDetalles(reporte.getDetalles(), null);

        return reporteProduccionJpaRepository.save(reporte);
    }

    @Override
    @Transactional
    public ReporteProduccionEntity actualizarReporte(UUID reporteId, ReporteProduccionEntity reporte) {
        ReporteProduccionEntity existente = reporteProduccionJpaRepository.findWithDetallesById(reporteId)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado."));

        produccionValidator.validarEditable(existente);
        produccionValidator.validarFechaNoFutura(reporte.getFechaReporte());

        existente.setFechaReporte(reporte.getFechaReporte());
        existente.setComentario(reporte.getComentario());
        existente.setUbicacionGps(reporte.getUbicacionGps());
        existente.setResponsableId(reporte.getResponsableId());

        existente.getDetalles().clear();
        if (reporte.getDetalles() != null) {
            for (DetalleRPCEntity detalle : reporte.getDetalles()) {
                detalle.setReporteProduccion(existente);
                existente.getDetalles().add(detalle);
            }
        }

        validarDetalles(existente.getDetalles(), existente.getId());

        return reporteProduccionJpaRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminarReporte(UUID reporteId) {
        ReporteProduccionEntity existente = reporteProduccionJpaRepository.findById(reporteId)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado."));

        produccionValidator.validarEditable(existente);
        reporteProduccionJpaRepository.delete(existente);
    }

    @Override
    @Transactional
    public ReporteProduccionEntity aprobarReporte(UUID reporteId, UUID usuarioAprobadorId) {
        ReporteProduccionEntity reporte = reporteProduccionJpaRepository.findById(reporteId)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado."));

        if (reporte.getEstado() != EstadoReporteProduccion.PENDIENTE) {
            // REGLA-006
            throw new BusinessRuleException("Solo se puede aprobar un reporte en estado PENDIENTE.");
        }

        reporte.setEstado(EstadoReporteProduccion.APROBADO);
        reporte.setAprobadorId(usuarioAprobadorId);
        return reporteProduccionJpaRepository.save(reporte);
    }

    @Override
    @Transactional
    public ReporteProduccionEntity rechazarReporte(UUID reporteId, UUID usuarioRechazoId, String motivo) {
        ReporteProduccionEntity reporte = reporteProduccionJpaRepository.findById(reporteId)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado."));

        if (reporte.getEstado() != EstadoReporteProduccion.PENDIENTE) {
            throw new BusinessRuleException("Solo se puede rechazar un reporte en estado PENDIENTE.");
        }
        if (motivo == null || motivo.isBlank()) {
            // REGLA-007
            throw new BusinessRuleException("El motivo de rechazo es obligatorio.");
        }

        reporte.setEstado(EstadoReporteProduccion.RECHAZADO);
        reporte.setAprobadorId(usuarioRechazoId);
        reporte.setComentario(motivo);
        return reporteProduccionJpaRepository.save(reporte);
    }

    private void validarDetalles(List<DetalleRPCEntity> detalles, UUID reporteIdExcluir) {
        if (detalles == null || detalles.isEmpty()) {
            // REGLA-005
            throw new BusinessRuleException("El reporte debe contener al menos un detalle.");
        }
        for (DetalleRPCEntity detalle : detalles) {
            produccionValidator.validarDetalle(detalle, reporteIdExcluir);
        }
    }
}
