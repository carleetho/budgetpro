package com.budgetpro.application.estimacion.port.in;

import java.util.UUID;

public interface EliminarEstimacionUseCase {
    void eliminar(UUID estimacionId);
}
