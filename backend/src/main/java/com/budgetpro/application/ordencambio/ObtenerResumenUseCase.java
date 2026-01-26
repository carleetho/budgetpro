package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioQueryPort;
import com.budgetpro.domain.finanzas.ordencambio.port.ResumenOrdenCambio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ObtenerResumenUseCase {

    private final OrdenCambioQueryPort ordenCambioQueryPort;

    public ObtenerResumenUseCase(OrdenCambioQueryPort ordenCambioQueryPort) {
        this.ordenCambioQueryPort = ordenCambioQueryPort;
    }

    @Transactional(readOnly = true)
    public ResumenOrdenCambio ejecutar(UUID proyectoId) {
        return ordenCambioQueryPort.getResumenByProyecto(proyectoId);
    }
}
