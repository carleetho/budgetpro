package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.port.in.AnularEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.service.EstimacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AnularEstimacionService implements AnularEstimacionUseCase {

    private final EstimacionService estimacionService;

    public AnularEstimacionService(EstimacionService estimacionService) {
        this.estimacionService = estimacionService;
    }

    @Override
    public void anular(UUID estimacionId, String motivo) {
        estimacionService.anular(EstimacionId.of(estimacionId), motivo);
    }
}
