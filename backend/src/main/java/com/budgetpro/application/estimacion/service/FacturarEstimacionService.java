package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.port.in.FacturarEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.service.EstimacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class FacturarEstimacionService implements FacturarEstimacionUseCase {

    private final EstimacionService estimacionService;

    public FacturarEstimacionService(EstimacionService estimacionService) {
        this.estimacionService = estimacionService;
    }

    @Override
    public void facturar(UUID estimacionId) {
        estimacionService.facturar(EstimacionId.of(estimacionId));
    }
}
