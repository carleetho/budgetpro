package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioFilters;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioQueryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListarOrdenesCambioUseCase {

    private final OrdenCambioQueryPort ordenCambioQueryPort;

    public ListarOrdenesCambioUseCase(OrdenCambioQueryPort ordenCambioQueryPort) {
        this.ordenCambioQueryPort = ordenCambioQueryPort;
    }

    @Transactional(readOnly = true)
    public List<OrdenCambio> ejecutar(UUID proyectoId, OrdenCambioFilters filters) {
        if (filters == null) {
            return ordenCambioQueryPort.findByProyectoId(proyectoId);
        } else {
            return ordenCambioQueryPort.findByProyectoIdAndFilters(proyectoId, filters);
        }
    }
}
