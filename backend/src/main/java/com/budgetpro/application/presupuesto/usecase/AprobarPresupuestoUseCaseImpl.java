package com.budgetpro.application.presupuesto.usecase;

import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoPuedeAprobarseException;
import com.budgetpro.application.presupuesto.port.in.AprobarPresupuestoUseCase;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.service.CalculoPresupuestoService;
import com.budgetpro.domain.finanzas.presupuesto.service.PresupuestoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso para aprobar un presupuesto.
 * 
 * **Estrategia de Transacciones:**
 * Este use case maneja la transaccionalidad del proceso de aprobación mediante @Transactional.
 * 
 * **Atomicidad del Baseline:**
 * La aprobación del presupuesto y el congelamiento del cronograma deben ser atómicos:
 * - Si la aprobación del presupuesto falla → rollback completo
 * - Si el congelamiento del cronograma falla → rollback completo (presupuesto no se aprueba)
 * - Si la persistencia falla → rollback completo
 * 
 * **Propagación de Transacciones:**
 * - Use case: @Transactional (REQUIRED por defecto) - inicia la transacción
 * - PresupuestoService.aprobar(): Sin @Transactional - participa en la transacción del use case
 * - CronogramaService.congelarPorPresupuesto(): Sin @Transactional - participa en la misma transacción
 * 
 * **Rollback Automático:**
 * Cualquier excepción no capturada (RuntimeException) provoca rollback automático de toda la transacción,
 * garantizando que no queden estados parciales (presupuesto aprobado sin cronograma congelado o viceversa).
 */
@Service
public class AprobarPresupuestoUseCaseImpl implements AprobarPresupuestoUseCase {

    private final PresupuestoService presupuestoService;
    private final CalculoPresupuestoService calculoPresupuestoService;

    public AprobarPresupuestoUseCaseImpl(PresupuestoService presupuestoService,
                                         CalculoPresupuestoService calculoPresupuestoService) {
        this.presupuestoService = presupuestoService;
        this.calculoPresupuestoService = calculoPresupuestoService;
    }

    @Override
    @Transactional(
            rollbackFor = Exception.class,
            propagation = org.springframework.transaction.annotation.Propagation.REQUIRED
    )
    public void aprobar(UUID presupuestoId) {
        // 1. Validar que todas las partidas hoja tengan APU
        if (!calculoPresupuestoService.todasLasPartidasHojaTienenAPU(presupuestoId)) {
            throw new PresupuestoNoPuedeAprobarseException(presupuestoId,
                    "No todas las partidas hoja tienen APU asociado");
        }

        // 2. Recalcular el presupuesto (validación implícita)
        // El cálculo se realiza al consultar el presupuesto, pero aquí validamos que sea posible
        calculoPresupuestoService.calcularCostoTotal(presupuestoId);

        // 3. Aprobar el presupuesto y congelar el cronograma simultáneamente (acoplamiento temporal)
        // PresupuestoService.aprobar() implementa el baseline principle:
        // - Aprueba el presupuesto (genera hashes de integridad)
        // - Congela el cronograma asociado (genera snapshot)
        // - Ambas operaciones son atómicas dentro de esta transacción
        // TODO: Obtener el ID del usuario actual del contexto de seguridad
        UUID approvedBy = UUID.randomUUID(); // Placeholder - debe obtenerse del contexto de seguridad
        PresupuestoId id = PresupuestoId.from(presupuestoId);
        
        try {
            presupuestoService.aprobar(id, approvedBy);
        } catch (IllegalStateException e) {
            // Presupuesto no encontrado o ya aprobado
            if (e.getMessage().contains("No existe un presupuesto")) {
                throw new PresupuestoNoEncontradoException(presupuestoId);
            }
            if (e.getMessage().contains("ya está aprobado")) {
                throw new PresupuestoNoPuedeAprobarseException(presupuestoId, "El presupuesto ya está aprobado");
            }
            throw new PresupuestoNoPuedeAprobarseException(presupuestoId, e.getMessage());
        }
    }
}
