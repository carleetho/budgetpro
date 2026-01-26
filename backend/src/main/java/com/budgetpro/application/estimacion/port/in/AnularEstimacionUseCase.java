package com.budgetpro.application.estimacion.port.in;

import java.util.UUID;

public interface AnularEstimacionUseCase {
    void anular(UUID estimacionId, String motivo);
}
