package com.budgetpro.application.finanzas.evm.listener;

import com.budgetpro.application.finanzas.evm.event.ValuacionCerradaEvent;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeriesId;
import com.budgetpro.domain.finanzas.evm.port.out.EVMDataProvider;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ValuacionCerradaEventListener {

    private static final Logger log = LoggerFactory.getLogger(ValuacionCerradaEventListener.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String MONEDA_DEFAULT = "USD";

    private final EVMTimeSeriesRepository repository;
    private final EVMDataProvider dataProvider;

    public ValuacionCerradaEventListener(
            EVMTimeSeriesRepository repository,
            EVMDataProvider dataProvider) {
        this.repository = repository;
        this.dataProvider = dataProvider;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onValuacionCerrada(ValuacionCerradaEvent event) {
        UUID proyectoId = event.proyectoId();
        LocalDateTime fechaCorte = event.fechaCorte().atStartOfDay();

        // 1) Carga de acumulados actuales
        BigDecimal pv = dataProvider.getPlannedValue(proyectoId, fechaCorte);
        BigDecimal ev = dataProvider.getEarnedValue(proyectoId, fechaCorte);
        BigDecimal ac = dataProvider.getActualCost(proyectoId, fechaCorte);
        BigDecimal bac = dataProvider.getBudgetAtCompletion(proyectoId);
        BigDecimal bacAjustado = dataProvider.getAdjustedBudgetAtCompletion(proyectoId);

        // 2) Carga de período previo con lock pesimista
        var previo = repository.findLatestWithLock(proyectoId);
        int nextPeriodo = previo.map(ts -> ts.getPeriodo() + 1).orElse(1);
        BigDecimal pvPrev = previo.map(EVMTimeSeries::getPvAcumulado).orElse(ZERO);
        BigDecimal evPrev = previo.map(EVMTimeSeries::getEvAcumulado).orElse(ZERO);
        BigDecimal acPrev = previo.map(EVMTimeSeries::getAcAcumulado).orElse(ZERO);

        // 3) Creación del modelo de dominio (calcula deltas internamente)
        EVMTimeSeries ts = EVMTimeSeries.crear(
                EVMTimeSeriesId.nuevo(),
                proyectoId,
                event.fechaCorte(),
                nextPeriodo,
                pv,
                ev,
                ac,
                bac,
                bacAjustado,
                pvPrev,
                evPrev,
                acPrev,
                MONEDA_DEFAULT);

        // 4) Persistencia con idempotencia
        try {
            repository.save(ts);
        } catch (DataIntegrityViolationException ex) {
            log.warn(
                    "Duplicate ValuacionCerradaEvent ignored for proyectoId={} fechaCorte={} createdBy={}",
                    proyectoId,
                    event.fechaCorte(),
                    SystemUser.SYSTEM_UUID);
        }
    }

    private static final class SystemUser {
        private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

        private SystemUser() {
        }
    }
}
