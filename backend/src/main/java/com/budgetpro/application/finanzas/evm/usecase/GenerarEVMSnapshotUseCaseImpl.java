package com.budgetpro.application.finanzas.evm.usecase;

import com.budgetpro.application.finanzas.evm.port.in.GenerarEVMSnapshotUseCase;
import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;
import com.budgetpro.domain.finanzas.evm.model.EVMSnapshotId;
import com.budgetpro.domain.finanzas.evm.port.out.EVMDataProvider;
import com.budgetpro.domain.finanzas.evm.port.out.EVMSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GenerarEVMSnapshotUseCaseImpl implements GenerarEVMSnapshotUseCase {

    private final EVMSnapshotRepository repository;
    private final EVMDataProvider dataProvider;

    public GenerarEVMSnapshotUseCaseImpl(EVMSnapshotRepository repository, EVMDataProvider dataProvider) {
        this.repository = repository;
        this.dataProvider = dataProvider;
    }

    @Override
    @Transactional
    public EVMSnapshot generar(UUID proyectoId) {
        LocalDateTime fechaCorte = LocalDateTime.now();

        // 1. Obtener métricas base desde los puertos de salida (Adapters de otros
        // módulos)
        BigDecimal bac = dataProvider.getBudgetAtCompletion(proyectoId);
        BigDecimal pv = dataProvider.getPlannedValue(proyectoId, fechaCorte);
        BigDecimal ev = dataProvider.getEarnedValue(proyectoId, fechaCorte);
        BigDecimal ac = dataProvider.getActualCost(proyectoId, fechaCorte);

        // 2. Crear el Snapshot (El modelo calcula internamente SPI, CPI, etc.)
        EVMSnapshot snapshot = EVMSnapshot.calcular(EVMSnapshotId.nuevo(), proyectoId, fechaCorte, pv, ev, ac, bac);

        // 3. Persistir
        repository.save(snapshot);

        return snapshot;
    }
}
