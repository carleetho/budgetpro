package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.dto.EstimacionItemResponse;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.port.in.ConsultarEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.exception.EstimacionNoEncontradaException;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionItem;
import com.budgetpro.domain.finanzas.estimacion.port.EstimacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ConsultarEstimacionService implements ConsultarEstimacionUseCase {

    private final EstimacionRepository estimacionRepository;

    public ConsultarEstimacionService(EstimacionRepository estimacionRepository) {
        this.estimacionRepository = estimacionRepository;
    }

    @Override
    public EstimacionResponse consultar(UUID estimacionId) {
        Estimacion estimacion = estimacionRepository.findById(EstimacionId.of(estimacionId))
                .orElseThrow(() -> new EstimacionNoEncontradaException(EstimacionId.of(estimacionId)));

        return mapToResponse(estimacion);
    }

    private EstimacionResponse mapToResponse(Estimacion estimacion) {
        EstimacionResponse response = new EstimacionResponse();
        response.setId(estimacion.getId().getValue());
        response.setPresupuestoId(estimacion.getPresupuestoId().getValue());
        response.setEstado(estimacion.getEstado().name());
        response.setFechaInicio(estimacion.getPeriodo().getFechaInicio());
        response.setFechaFin(estimacion.getPeriodo().getFechaFin());
        response.setRetencionPorcentaje(estimacion.getRetencionPorcentaje().getValue());
        response.setMontoRetencion(estimacion.calcularMontoRetencion().getValue());
        response.setSubtotal(estimacion.calcularSubtotal().getValue());
        response.setTotalPagar(estimacion.calcularTotalPagar().getValue());

        List<EstimacionItemResponse> itemResponses = estimacion.getItems().stream().map(this::mapItemToResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    private EstimacionItemResponse mapItemToResponse(EstimacionItem item) {
        EstimacionItemResponse response = new EstimacionItemResponse();
        response.setId(item.getId().getValue());
        response.setPartidaId(item.getPartidaId());
        response.setConcepto(item.getConcepto());
        response.setMontoContractual(item.getMontoContractual().getValue());
        response.setPorcentajeAnterior(item.getPorcentajeAnterior().getValue());
        response.setMontoAnterior(item.getMontoAnterior().getValue());
        response.setPorcentajeActual(item.getPorcentajeActual().getValue());
        response.setMontoActual(item.getMontoActual().getValue());
        response.setPorcentajeAcumulado(item.getPorcentajeAcumulado().getValue());
        response.setMontoAcumulado(item.getMontoAcumulado().getValue());
        response.setSaldoPorEjercer(item.getSaldoPorEjercer().getValue());
        return response;
    }
}
