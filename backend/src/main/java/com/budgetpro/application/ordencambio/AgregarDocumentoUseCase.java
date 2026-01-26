package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.exception.OrdenCambioException;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioDocumento;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.model.TipoDocumentoOrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AgregarDocumentoUseCase {

    private final OrdenCambioRepository ordenCambioRepository;

    public AgregarDocumentoUseCase(OrdenCambioRepository ordenCambioRepository) {
        this.ordenCambioRepository = ordenCambioRepository;
    }

    @Transactional
    public OrdenCambio ejecutar(UUID ordenCambioId, TipoDocumentoOrdenCambio tipo, String nombreArchivo,
            String rutaArchivo, UUID subidoPorId) {

        OrdenCambioId id = OrdenCambioId.from(ordenCambioId);
        OrdenCambio orden = ordenCambioRepository.findById(id)
                .orElseThrow(() -> new OrdenCambioException("Orden de cambio no encontrada: " + ordenCambioId));

        OrdenCambioDocumento documento = OrdenCambioDocumento.crear(id, tipo, nombreArchivo, rutaArchivo, subidoPorId);

        orden.agregarDocumento(documento);

        return ordenCambioRepository.save(orden);
    }
}
