package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.exception.OrdenCambioException;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EnviarARevisionUseCase {

    private final OrdenCambioRepository ordenCambioRepository;

    public EnviarARevisionUseCase(OrdenCambioRepository ordenCambioRepository) {
        this.ordenCambioRepository = ordenCambioRepository;
    }

    @Transactional
    public OrdenCambio ejecutar(UUID ordenCambioId, String justificacionTecnica) {
        OrdenCambio orden = ordenCambioRepository.findById(OrdenCambioId.from(ordenCambioId))
                .orElseThrow(() -> new OrdenCambioException("Orden de cambio no encontrada: " + ordenCambioId));

        orden.enviarARevision(justificacionTecnica);

        return ordenCambioRepository.save(orden);
    }
}
