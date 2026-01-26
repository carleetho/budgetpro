package com.budgetpro.application.estimacion.port.in;

import java.util.UUID;

public interface FacturarEstimacionUseCase {
    void facturar(UUID estimacionId);
}
