package com.budgetpro.application.estimacion.port.in;

import java.util.UUID;

public interface AprobarEstimacionUseCase {
    void aprobar(UUID estimacionId, UUID aprobadoPor);
}
