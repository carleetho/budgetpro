package com.budgetpro.application.estimacion.usecase;

import com.budgetpro.application.estimacion.dto.DetalleEstimacionResponse;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.port.in.ConsultarEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultarEstimacionUseCaseImpl implements ConsultarEstimacionUseCase {

    private final EstimacionRepository estimacionRepository;

    public ConsultarEstimacionUseCaseImpl(EstimacionRepository estimacionRepository) {
        this.estimacionRepository = estimacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EstimacionResponse obtenerPorId(UUID estimacionId) {
        Estimacion estimacion = estimacionRepository.findById(EstimacionId.of(estimacionId))
                .orElseThrow(() -> new IllegalArgumentException("Estimación no encontrada: " + estimacionId));
        return toResponse(estimacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstimacionResponse> listarPorProyecto(UUID proyectoId) {
        return estimacionRepository.findByProyectoId(proyectoId).stream()
                .map(ConsultarEstimacionUseCaseImpl::toResponse)
                .collect(Collectors.toList());
    }

    static EstimacionResponse toResponse(Estimacion estimacion) {
        List<DetalleEstimacionResponse> detallesResponse = estimacion.getDetalles().stream()
                .map(detalle -> new DetalleEstimacionResponse(
                        detalle.getId().getValue(),
                        detalle.getPartidaId(),
                        detalle.getCantidadAvance(),
                        detalle.getPrecioUnitario(),
                        detalle.getImporte(),
                        detalle.getAcumuladoAnterior()
                ))
                .toList();

        return new EstimacionResponse(
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
                detallesResponse,
                estimacion.getVersion() != null ? estimacion.getVersion().intValue() : null
        );
    }
}

