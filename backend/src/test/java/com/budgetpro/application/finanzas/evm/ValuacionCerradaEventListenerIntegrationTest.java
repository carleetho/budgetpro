package com.budgetpro.application.finanzas.evm;

import com.budgetpro.application.finanzas.evm.event.ValuacionCerradaEvent;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class ValuacionCerradaEventListenerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EVMTimeSeriesRepository timeSeriesRepository;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();

        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(proyectoId);
        proyecto.setNombre("REQ61-LISTENER-" + proyectoId);
        proyecto.setUbicacion("Test");
        proyecto.setEstado(EstadoProyecto.ACTIVO);
        proyecto.setMoneda("USD");
        proyecto.setPresupuestoTotal(new BigDecimal("1000.0000"));
        proyecto.setVersion(0);
        proyectoJpaRepository.save(proyecto);
    }

    @Test
    void testAC_TD_01_firstEventCreatesRow() {
        publishInCommittedTransaction(new ValuacionCerradaEvent(
                proyectoId,
                "PER-001",
                LocalDate.of(2026, 3, 1)));

        var rows = timeSeriesRepository.findByProyectoId(proyectoId, null, null);
        assertThat(rows).hasSize(1);

        var ts = rows.get(0);
        assertThat(ts.getPeriodo()).isEqualTo(1);
        assertThat(ts.getPvAcumulado()).isEqualByComparingTo("1");
        assertThat(ts.getEvAcumulado()).isEqualByComparingTo("1");
        assertThat(ts.getAcAcumulado()).isEqualByComparingTo("1");
    }

    @Test
    void testAC_TD_02_duplicateEventIsIgnored(CapturedOutput output) {
        ValuacionCerradaEvent duplicate = new ValuacionCerradaEvent(
                proyectoId,
                "PER-001",
                LocalDate.of(2026, 3, 2));

        publishInCommittedTransaction(duplicate);
        publishInCommittedTransaction(duplicate);

        var rows = timeSeriesRepository.findByProyectoId(
                proyectoId,
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 3, 2));

        assertThat(rows).hasSize(1);
        assertThat(output.getOut()).contains("Duplicate ValuacionCerradaEvent ignored");
    }

    @Test
    void testAC_TD_03_periodoIncrementsCorrectly() {
        publishInCommittedTransaction(new ValuacionCerradaEvent(proyectoId, "PER-001", LocalDate.of(2026, 3, 3)));
        publishInCommittedTransaction(new ValuacionCerradaEvent(proyectoId, "PER-002", LocalDate.of(2026, 3, 4)));
        publishInCommittedTransaction(new ValuacionCerradaEvent(proyectoId, "PER-003", LocalDate.of(2026, 3, 5)));

        List<Integer> periodos = timeSeriesRepository.findByProyectoId(proyectoId, null, null)
                .stream()
                .map(ts -> ts.getPeriodo())
                .toList();

        assertThat(periodos).containsExactly(1, 2, 3);
    }

    @SuppressWarnings("null")
    private void publishInCommittedTransaction(ValuacionCerradaEvent event) {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent((Object) event));
    }
}
