package com.budgetpro.infrastructure.logistica.inventario.adapter;

import com.budgetpro.domain.logistica.inventario.event.BudgetAlertEvent;
import com.budgetpro.domain.logistica.inventario.port.out.BudgetAlertPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BudgetAlertPublisherAdapter implements BudgetAlertPublisher {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertPublisherAdapter.class);

    @Override
    public void publicar(BudgetAlertEvent event) {
        log.info("Budget alert published: {}", event);
    }
}
