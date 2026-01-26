package com.budgetpro.domain.finanzas.estimacion.exception;

import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.model.EstadoEstimacion;

public class EstimacionCongeladaException extends RuntimeException {

    public EstimacionCongeladaException(EstimacionId id, EstadoEstimacion estado) {
        super(String.format("La estimación %s no puede ser modificada porque está en estado %s", id.getValue(),
                estado));
    }
}
