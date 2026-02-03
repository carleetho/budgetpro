package com.budgetpro.infrastructure.logistica.inventario.adapter;

import com.budgetpro.domain.logistica.inventario.event.MaterialConsumed;
import com.budgetpro.domain.logistica.inventario.port.out.AcPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AcPublisherAdapter implements AcPublisher {

    private static final Logger log = LoggerFactory.getLogger(AcPublisherAdapter.class);

    @Override
    public void publicar(MaterialConsumed event) {
        log.info("Event published: {}", event);
    }
}
