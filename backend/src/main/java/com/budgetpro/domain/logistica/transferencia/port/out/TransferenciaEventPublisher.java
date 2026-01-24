package com.budgetpro.domain.logistica.transferencia.port.out;

import com.budgetpro.domain.logistica.transferencia.event.MaterialTransferredBetweenProjects;

public interface TransferenciaEventPublisher {
    void publicar(MaterialTransferredBetweenProjects event);
}
