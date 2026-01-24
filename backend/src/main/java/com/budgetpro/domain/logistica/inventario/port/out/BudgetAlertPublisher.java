package com.budgetpro.domain.logistica.inventario.port.out;

import com.budgetpro.domain.logistica.inventario.event.BudgetAlertEvent;

public interface BudgetAlertPublisher {
    void publicar(BudgetAlertEvent event);
}
