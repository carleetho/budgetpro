package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.dto.CrearEstimacionCommand;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.port.in.CrearEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.PeriodoEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.RetencionPorcentaje;
import com.budgetpro.domain.finanzas.estimacion.service.EstimacionService;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
public class CrearEstimacionService implements CrearEstimacionUseCase {

    private final EstimacionService estimacionService;
    // Note: In hexagonal/clean arch, we might use a mapper to convert domain to
    // DTO.
    // For simplicity, I'll map manually or assume a helper.
    // Let's map manually here to keep it self-contained.

    public CrearEstimacionService(EstimacionService estimacionService) {
        this.estimacionService = estimacionService;
    }

    @Override
    public EstimacionResponse crear(CrearEstimacionCommand command) {
        Estimacion estimacion = estimacionService.crear(command.getProyectoId(),
                PresupuestoId.of(command.getPresupuestoId()),
                PeriodoEstimacion.of(command.getFechaInicio(), command.getFechaFin()),
                RetencionPorcentaje.of(command.getRetencionPorcentaje()));

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
        response.setItems(Collections.emptyList()); // New estimation has no items
        return response;
    }
}
