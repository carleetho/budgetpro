package com.budgetpro.infrastructure.persistence.adapter.evm;

import com.budgetpro.domain.finanzas.evm.port.out.EVMDataProvider;
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

    @Override
    public BigDecimal getBudgetAtCompletion(UUID proyectoId) {
        // TODO: Conectar con PresupuestoRepository
        return BigDecimal.TEN; // Placeholder
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
