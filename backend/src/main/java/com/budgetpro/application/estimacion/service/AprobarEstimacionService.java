package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.port.in.AprobarEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.service.EstimacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AprobarEstimacionService implements AprobarEstimacionUseCase {

    private final EstimacionService estimacionService;

    public AprobarEstimacionService(EstimacionService estimacionService) {
        this.estimacionService = estimacionService;
    }

    @Override
    public void aprobar(UUID estimacionId, UUID aprobadoPor) {
        estimacionService.aprobar(EstimacionId.of(estimacionId), aprobadoPor);
    }
}
