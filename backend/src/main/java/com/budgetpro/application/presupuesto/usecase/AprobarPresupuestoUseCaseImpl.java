package com.budgetpro.application.presupuesto.usecase;

import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoPuedeAprobarseException;
import com.budgetpro.application.presupuesto.port.in.AprobarPresupuestoUseCase;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.CalculoPresupuestoService;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso para aprobar un presupuesto.
 */
@Service
public class AprobarPresupuestoUseCaseImpl implements AprobarPresupuestoUseCase {

    private final PresupuestoRepository presupuestoRepository;
    private final CalculoPresupuestoService calculoPresupuestoService;
    private final IntegrityHashService integrityHashService;

    public AprobarPresupuestoUseCaseImpl(PresupuestoRepository presupuestoRepository,
                                         CalculoPresupuestoService calculoPresupuestoService,
                                         IntegrityHashService integrityHashService) {
        this.presupuestoRepository = presupuestoRepository;
        this.calculoPresupuestoService = calculoPresupuestoService;
        this.integrityHashService = integrityHashService;
    }

    @Override
    @Transactional
    public void aprobar(UUID presupuestoId) {
        // 1. Buscar el presupuesto
        PresupuestoId id = PresupuestoId.from(presupuestoId);
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNoEncontradoException(presupuestoId));

        // 2. Validar que el presupuesto no esté ya aprobado
        if (presupuesto.isAprobado()) {
            throw new PresupuestoNoPuedeAprobarseException(presupuestoId, "El presupuesto ya está aprobado");
        }

        // 3. Validar que todas las partidas hoja tengan APU
        if (!calculoPresupuestoService.todasLasPartidasHojaTienenAPU(presupuestoId)) {
            throw new PresupuestoNoPuedeAprobarseException(presupuestoId,
                    "No todas las partidas hoja tienen APU asociado");
        }

        // 4. Recalcular el presupuesto (validación implícita)
        // El cálculo se realiza al consultar el presupuesto, pero aquí validamos que sea posible
        calculoPresupuestoService.calcularCostoTotal(presupuestoId);

        // 5. Aprobar el presupuesto (cambia estado a CONGELADO, marca como contractual y genera hashes de integridad)
        // TODO: Obtener el ID del usuario actual del contexto de seguridad
        UUID approvedBy = UUID.randomUUID(); // Placeholder - debe obtenerse del contexto de seguridad
        presupuesto.aprobar(approvedBy, integrityHashService);

        // 6. Persistir los cambios
        presupuestoRepository.save(presupuesto);
    }
}
