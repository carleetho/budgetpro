package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.exception.OrdenCambioException;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RechazarOrdenCambioUseCase {

    private final OrdenCambioRepository ordenCambioRepository;

    // Aquí también se verificaría permiso de aprobador

    public RechazarOrdenCambioUseCase(OrdenCambioRepository ordenCambioRepository) {
        this.ordenCambioRepository = ordenCambioRepository;
    }

    @Transactional
    public OrdenCambio ejecutar(UUID ordenCambioId, UUID rechazadorId, String motivoRechazo) {
        OrdenCambio orden = ordenCambioRepository.findById(OrdenCambioId.from(ordenCambioId))
                .orElseThrow(() -> new OrdenCambioException("Orden de cambio no encontrada: " + ordenCambioId));

        orden.rechazar(rechazadorId, motivoRechazo);

        return ordenCambioRepository.save(orden);
    }
}
