package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.exception.OrdenCambioException;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioRecurso;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioRepository;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AgregarRecursoUseCase {

    private final OrdenCambioRepository ordenCambioRepository;

    public AgregarRecursoUseCase(OrdenCambioRepository ordenCambioRepository) {
        this.ordenCambioRepository = ordenCambioRepository;
    }

    @Transactional
    public OrdenCambio ejecutar(UUID ordenCambioId, UUID recursoId, String externalRecursoId, TipoRecurso tipo,
            String descripcion, String unidad, BigDecimal cantidad, BigDecimal precioUnitario) {

        OrdenCambioId id = OrdenCambioId.from(ordenCambioId);
        OrdenCambio orden = ordenCambioRepository.findById(id)
                .orElseThrow(() -> new OrdenCambioException("Orden de cambio no encontrada: " + ordenCambioId));

        OrdenCambioRecurso recurso = OrdenCambioRecurso.crear(id, recursoId != null ? RecursoId.of(recursoId) : null,
                externalRecursoId, descripcion, tipo, cantidad, unidad);

        orden.agregarRecurso(recurso);

        return ordenCambioRepository.save(orden);
    }
}
