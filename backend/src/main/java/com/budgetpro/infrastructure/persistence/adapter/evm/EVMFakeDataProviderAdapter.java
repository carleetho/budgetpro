package com.budgetpro.infrastructure.persistence.adapter.evm;

import com.budgetpro.domain.finanzas.evm.port.out.EVMDataProvider;
import com.budgetpro.infrastructure.persistence.entity.cambio.EstadoOrdenCambio;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Adaptador temporal para proveer datos a EVM. TODO: Implementar integración
 * real con módulos de Presupuesto, Avance y Costos via Repositories o Services.
 */
@Component
public class EVMFakeDataProviderAdapter implements EVMDataProvider {

    private static final BigDecimal DEFAULT_BAC = BigDecimal.TEN;
    private final EntityManager entityManager;

    public EVMFakeDataProviderAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public BigDecimal getBudgetAtCompletion(UUID proyectoId) {
        BigDecimal bac = entityManager.createQuery(
                "SELECT p.presupuestoTotal FROM ProyectoEntity p WHERE p.id = :proyectoId",
                BigDecimal.class)
                .setParameter("proyectoId", proyectoId)
                .getResultStream()
                .findFirst()
                .orElse(null);

        return bac != null ? bac : DEFAULT_BAC;
    }

    @Override
    public BigDecimal getAdjustedBudgetAtCompletion(UUID proyectoId) {
        BigDecimal bac = getBudgetAtCompletion(proyectoId);
        BigDecimal approvedChangeOrders = entityManager.createQuery(
                "SELECT COALESCE(SUM(oc.impactoPresupuesto), 0) "
                        + "FROM OrdenCambioEntity oc "
                        + "WHERE oc.proyecto.id = :proyectoId "
                        + "AND oc.estado = :estado",
                BigDecimal.class)
                .setParameter("proyectoId", proyectoId)
                .setParameter("estado", EstadoOrdenCambio.APROBADO)
                .getSingleResult();

        return bac.add(approvedChangeOrders);
    }

    @Override
    public BigDecimal getPlannedValue(UUID proyectoId, LocalDateTime fechaCorte) {
        // TODO: Conectar con Cronograma/AvanceProgramado
        return BigDecimal.ONE; // Placeholder
    }

    @Override
    public BigDecimal getEarnedValue(UUID proyectoId, LocalDateTime fechaCorte) {
        // TODO: Conectar con AvanceFisicoRepository
        return BigDecimal.ONE; // Placeholder
    }

    @Override
    public BigDecimal getActualCost(UUID proyectoId, LocalDateTime fechaCorte) {
        // TODO: Conectar con Modulo de Gastos/Costos
        return BigDecimal.ONE; // Placeholder
    }
}
