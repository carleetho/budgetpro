package com.budgetpro.application.finanzas.evm;

import com.budgetpro.application.finanzas.evm.event.ValuacionCerradaEvent;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Import(EVMTimeSeriesConcurrencyIntegrationTest.HibernateSqlCaptureConfig.class)
class EVMTimeSeriesConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EVMTimeSeriesRepository timeSeriesRepository;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    private SqlStatementCollector sqlCollector;

    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();

        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(proyectoId);
        proyecto.setNombre("REQ61-CONC-" + proyectoId);
        proyecto.setUbicacion("Test");
        proyecto.setEstado(EstadoProyecto.ACTIVO);
        proyecto.setMoneda("USD");
        proyecto.setPresupuestoTotal(new BigDecimal("1000.0000"));
        proyecto.setVersion(0);
        proyectoJpaRepository.save(proyecto);
    }

    @Test
    void testAC_CONC_01_pessimisticLockPreventsDuplicatePeriodo() throws Exception {
        publishInCommittedTransaction(new ValuacionCerradaEvent(
                proyectoId,
                "BASE",
                LocalDate.of(2026, 3, 10)));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        Future<?> f1 = executor.submit(() -> {
            await(startLatch);
            publishInCommittedTransaction(new ValuacionCerradaEvent(
                    proyectoId,
                    "CONC-A",
                    LocalDate.of(2026, 3, 11)));
        });

        Future<?> f2 = executor.submit(() -> {
            await(startLatch);
            publishInCommittedTransaction(new ValuacionCerradaEvent(
                    proyectoId,
                    "CONC-B",
                    LocalDate.of(2026, 3, 12)));
        });

        startLatch.countDown();
        f1.get();
        f2.get();
        executor.shutdown();

        var allRows = timeSeriesRepository.findByProyectoId(proyectoId, null, null);
        assertThat(allRows).hasSize(3);
        assertThat(allRows.stream().map(ts -> ts.getPeriodo())).containsExactly(1, 2, 3);

        var concurrentRows = timeSeriesRepository.findByProyectoId(
                        proyectoId,
                        LocalDate.of(2026, 3, 11),
                        LocalDate.of(2026, 3, 12))
                .stream()
                .map(ts -> ts.getPeriodo())
                .toList();

        assertThat(concurrentRows).hasSize(2);
        assertThat(concurrentRows).containsExactly(2, 3);
    }

    @Test
    void testAC_CONC_02_findLatestWithLockEmitsSelectForUpdate() {
        publishInCommittedTransaction(new ValuacionCerradaEvent(
                proyectoId,
                "BASE",
                LocalDate.of(2026, 3, 20)));

        sqlCollector.clear();
        transactionTemplate.executeWithoutResult(status -> timeSeriesRepository.findLatestWithLock(proyectoId));

        boolean hasForUpdate = sqlCollector.getStatements().stream()
                .map(sql -> sql.toLowerCase(Locale.ROOT))
                .anyMatch(sql -> sql.contains("for update"));

        assertThat(hasForUpdate).isTrue();
    }

    @SuppressWarnings("null")
    private void publishInCommittedTransaction(ValuacionCerradaEvent event) {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent((Object) event));
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while waiting for latch", e);
        }
    }

    @TestConfiguration
    static class HibernateSqlCaptureConfig {
        @Bean
        SqlStatementCollector sqlStatementCollector() {
            return new SqlStatementCollector();
        }

        @Bean
        HibernatePropertiesCustomizer hibernatePropertiesCustomizer(SqlStatementCollector collector) {
            return properties -> properties.put("hibernate.session_factory.statement_inspector", collector);
        }
    }

    static class SqlStatementCollector implements StatementInspector {
        private final java.util.concurrent.CopyOnWriteArrayList<String> statements =
                new java.util.concurrent.CopyOnWriteArrayList<>();

        @Override
        public String inspect(String sql) {
            statements.add(sql);
            return sql;
        }

        List<String> getStatements() {
            return List.copyOf(statements);
        }

        void clear() {
            statements.clear();
        }
    }
}
