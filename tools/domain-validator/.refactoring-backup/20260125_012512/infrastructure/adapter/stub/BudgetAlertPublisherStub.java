package com.budgetpro.infrastructure.adapter.stub;

import com.budgetpro.domain.logistica.inventario.event.BudgetAlertEvent;
import com.budgetpro.domain.logistica.inventario.port.out.BudgetAlertPublisher;
import org.springframework.stereotype.Component;

@Component
public class BudgetAlertPublisherStub implements BudgetAlertPublisher {
    @Override
    public void publicar(BudgetAlertEvent event) {
        // No-op
    }
}
