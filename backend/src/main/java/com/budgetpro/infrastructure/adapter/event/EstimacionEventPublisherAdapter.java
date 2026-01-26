package com.budgetpro.infrastructure.adapter.event;

import com.budgetpro.domain.finanzas.estimacion.port.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adaptador de infraestructura que implementa EventPublisher usando Spring
 * ApplicationEventPublisher.
 */
@Component
public class EstimacionEventPublisherAdapter implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EstimacionEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher,
                "ApplicationEventPublisher no puede ser nulo");
    }

    @Override
    public void publish(Object event) {
        if (event != null) {
            applicationEventPublisher.publishEvent(event);
        }
    }
}
