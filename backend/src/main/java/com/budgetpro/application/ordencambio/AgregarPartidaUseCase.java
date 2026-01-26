package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.exception.OrdenCambioException;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioPartida;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioRepository;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AgregarPartidaUseCase {

    private final OrdenCambioRepository ordenCambioRepository;
    // Necesitaria APU/Partida Repository para validar existencia real, pero por
    // ahora solo IDs.

    public AgregarPartidaUseCase(OrdenCambioRepository ordenCambioRepository) {
        this.ordenCambioRepository = ordenCambioRepository;
    }

    @Transactional
    public OrdenCambio ejecutar(UUID ordenCambioId, UUID partidaId, String item, String descripcion, String unidad,
            BigDecimal metrado, BigDecimal precioUnitario, Long apuSnapshotId) {

        OrdenCambioId id = OrdenCambioId.from(ordenCambioId);
        OrdenCambio orden = ordenCambioRepository.findById(id)
                .orElseThrow(() -> new OrdenCambioException("Orden de cambio no encontrada: " + ordenCambioId));

        OrdenCambioPartida partida = OrdenCambioPartida.crear(id, partidaId != null ? PartidaId.from(partidaId) : null, // Asumiendo
                                                                                                                        // wrapper
                                                                                                                        // //
                                                                                                                        // constructor
                // PartidaId(UUID)
                // o
                // similar
                item, descripcion, unidad, metrado, precioUnitario, apuSnapshotId);

        orden.agregarPartida(partida);

        return ordenCambioRepository.save(orden);
    }
}
