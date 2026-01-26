package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.port.in.EliminarEstimacionUseCase;
import com.budgetpro.domain.finanzas.estimacion.exception.EstimacionCongeladaException;
import com.budgetpro.domain.finanzas.estimacion.exception.EstimacionNoEncontradaException;
import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;
import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.EstimacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class EliminarEstimacionService implements EliminarEstimacionUseCase {

    private final EstimacionRepository estimacionRepository;

    public EliminarEstimacionService(EstimacionRepository estimacionRepository) {
        this.estimacionRepository = estimacionRepository;
    }

    @Override
    public void eliminar(UUID estimacionId) {
        EstimacionId id = EstimacionId.of(estimacionId);
        Estimacion estimacion = estimacionRepository.findById(id)
                .orElseThrow(() -> new EstimacionNoEncontradaException(id));

        if (estimacion.getEstado() != EstadoEstimacion.BORRADOR) {
            throw new EstimacionCongeladaException(id, estimacion.getEstado());
        }

        estimacionRepository.delete(id);
    }
}
