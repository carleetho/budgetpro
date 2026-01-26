package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.exception.OrdenCambioException;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioQueryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ConsultarOrdenCambioUseCase {

    private final OrdenCambioQueryPort ordenCambioQueryPort;

    public ConsultarOrdenCambioUseCase(OrdenCambioQueryPort ordenCambioQueryPort) {
        this.ordenCambioQueryPort = ordenCambioQueryPort;
    }

    @Transactional(readOnly = true)
    public OrdenCambio ejecutar(UUID ordenCambioId) {
        return ordenCambioQueryPort.findByIdWithDetails(OrdenCambioId.from(ordenCambioId))
                .orElseThrow(() -> new OrdenCambioException("Orden de cambio no encontrada: " + ordenCambioId));
    }
}
