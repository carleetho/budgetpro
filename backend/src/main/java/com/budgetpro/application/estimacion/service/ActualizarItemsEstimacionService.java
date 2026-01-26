package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.dto.ActualizarItemsCommand;
import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.port.in.ActualizarItemsEstimacionUseCase;
import com.budgetpro.application.estimacion.port.in.ConsultarEstimacionUseCase; // Reuse mapping logic via use case or separate mapper?
// Let's reuse existing service mapper logic if possible or duplicate for independence.
// Or inject ConsultarEstimacionService to return response.
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.MontoEstimado;
import com.budgetpro.domain.finanzas.estimacion.model.PorcentajeAvance;
import com.budgetpro.domain.finanzas.estimacion.service.EstimacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ActualizarItemsEstimacionService implements ActualizarItemsEstimacionUseCase {

    private final EstimacionService estimacionService;
    private final ConsultarEstimacionUseCase consultarEstimacionUseCase;

    public ActualizarItemsEstimacionService(EstimacionService estimacionService,
            ConsultarEstimacionUseCase consultarEstimacionUseCase) {
        this.estimacionService = estimacionService;
        this.consultarEstimacionUseCase = consultarEstimacionUseCase;
    }

    @Override
    public EstimacionResponse actualizarItems(UUID estimacionId, ActualizarItemsCommand command) {
        EstimacionId id = EstimacionId.of(estimacionId);

        for (ActualizarItemsCommand.ItemUpdate itemUpdate : command.getItems()) {
            estimacionService.agregarItem(id, itemUpdate.getPartidaId(), itemUpdate.getConcepto(),
                    MontoEstimado.of(itemUpdate.getMontoContractual()),
                    PorcentajeAvance.of(itemUpdate.getPorcentajeAvancePeriodo()));
        }

        return consultarEstimacionUseCase.consultar(estimacionId);
    }
}
