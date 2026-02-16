package com.budgetpro.application.compra.usecase;

import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.domain.logistica.compra.port.in.AprobarOrdenCompraUseCase;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del caso de uso para aprobar una orden de compra.
 */
@Service
public class AprobarOrdenCompraUseCaseImpl implements AprobarOrdenCompraUseCase {

    private final OrdenCompraRepository ordenCompraRepository;

    public AprobarOrdenCompraUseCaseImpl(OrdenCompraRepository ordenCompraRepository) {
        this.ordenCompraRepository = ordenCompraRepository;
    }

    @Override
    @Transactional
    public void aprobar(OrdenCompraId ordenCompraId, java.util.UUID userId) {
        // 1. Cargar orden de compra
        OrdenCompra ordenCompra = ordenCompraRepository.findById(ordenCompraId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Orden de compra no encontrada: %s", ordenCompraId)
                ));

        // 2. Aprobar orden (SOLICITADA → APROBADA)
        LocalDateTime now = LocalDateTime.now();
        ordenCompra.aprobar(userId, now);

        // 3. Persistir cambios
        ordenCompraRepository.save(ordenCompra);
    }
}
