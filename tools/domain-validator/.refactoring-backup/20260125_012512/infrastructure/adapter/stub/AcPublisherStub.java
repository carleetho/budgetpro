package com.budgetpro.infrastructure.adapter.stub;

import com.budgetpro.domain.logistica.inventario.event.MaterialConsumed;
import com.budgetpro.domain.logistica.inventario.port.out.AcPublisher;
import org.springframework.stereotype.Component;

@Component
public class AcPublisherStub implements AcPublisher {
    @Override
    public void publicar(MaterialConsumed event) {
        // No-op
    }
}
