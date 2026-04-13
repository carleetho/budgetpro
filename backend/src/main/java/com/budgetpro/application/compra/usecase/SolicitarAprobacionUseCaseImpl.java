package com.budgetpro.application.compra.usecase;

import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.domain.logistica.compra.port.in.SolicitarAprobacionUseCase;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import com.budgetpro.domain.logistica.compra.port.out.PartidaValidator;
import com.budgetpro.domain.logistica.compra.port.out.PresupuestoValidator;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del caso de uso para solicitar aprobación de una orden de compra.
 */
@Service
public class SolicitarAprobacionUseCaseImpl implements SolicitarAprobacionUseCase {

    private final OrdenCompraRepository ordenCompraRepository;
    private final PresupuestoValidator presupuestoValidator;
    private final PartidaValidator partidaValidator;
    private final ProveedorValidator proveedorValidator;

    public SolicitarAprobacionUseCaseImpl(OrdenCompraRepository ordenCompraRepository,
                                         PresupuestoValidator presupuestoValidator,
                                         @Qualifier("compraPartidaValidatorAdapter") PartidaValidator partidaValidator,
                                         ProveedorValidator proveedorValidator) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.presupuestoValidator = presupuestoValidator;
        this.partidaValidator = partidaValidator;
        this.proveedorValidator = proveedorValidator;
    }

    @Override
    @Transactional
    public void solicitar(OrdenCompraId ordenCompraId, java.util.UUID userId) {
        // 1. Cargar orden de compra
        OrdenCompra ordenCompra = ordenCompraRepository.findById(ordenCompraId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Orden de compra no encontrada: %s", ordenCompraId)
                ));

        // 2. Solicitar aprobación (valida L-01, L-04, REGLA-153)
        LocalDateTime now = LocalDateTime.now();
        ordenCompra.solicitar(presupuestoValidator, partidaValidator, proveedorValidator, userId, now);

        // 4. Persistir cambios
        ordenCompraRepository.save(ordenCompra);
    }
}
