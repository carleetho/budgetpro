package com.budgetpro.infrastructure.scheduler;

import com.budgetpro.application.finanzas.evm.port.in.CerrarPeriodoUseCase;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests para EVMPeriodoCierreScheduler (REQ-64, AC-E04-INV-04).
 */
@ExtendWith(MockitoExtension.class)
class EVMPeriodoCierreSchedulerTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private EVMTimeSeriesRepository evmTimeSeriesRepository;

    @Mock
    private CerrarPeriodoUseCase cerrarPeriodoUseCase;

    private EVMPeriodoCierreScheduler scheduler;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        scheduler = new EVMPeriodoCierreScheduler(
                proyectoRepository,
                evmTimeSeriesRepository,
                cerrarPeriodoUseCase);

        Logger logger = (Logger) LoggerFactory.getLogger(EVMPeriodoCierreScheduler.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.detachAndStopAllAppenders();
        logger.addAppender(logAppender);
        logger.setLevel(Level.ALL);
    }

    @Nested
    @DisplayName("P1: MENSUAL aligned today, no evm_time_series row")
    class P1AlignedNoExisting {

        @Test
        @DisplayName("cerrar() llamado una vez")
        void cerrarLlamadoUnaVez() {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            ProyectoId p1Id = ProyectoId.nuevo();
            Proyecto p1 = Proyecto.reconstruir(
                    p1Id, "P1", "Loc", EstadoProyecto.ACTIVO,
                    today.minusMonths(1).atStartOfDay(), FrecuenciaControl.MENSUAL);

            when(proyectoRepository.findAllWithFrecuenciaControl()).thenReturn(List.of(p1));
            when(evmTimeSeriesRepository.existsByProyectoIdAndFechaCorte(p1Id.getValue(), today)).thenReturn(false);

            scheduler.cerrarPeriodosVencidos();

            ArgumentCaptor<UUID> proyectoIdCaptor = ArgumentCaptor.forClass(UUID.class);
            ArgumentCaptor<LocalDate> fechaCaptor = ArgumentCaptor.forClass(LocalDate.class);
            verify(cerrarPeriodoUseCase).cerrar(proyectoIdCaptor.capture(), fechaCaptor.capture());
            assertThat(proyectoIdCaptor.getValue()).isEqualTo(p1Id.getValue());
            assertThat(fechaCaptor.getValue()).isEqualTo(today);
        }
    }

    @Nested
    @DisplayName("P2: null frecuenciaControl")
    class P2SinFrecuencia {

        @Test
        @DisplayName("no retornado por findAllWithFrecuenciaControl, cerrar() no llamado")
        void cerrarNoLlamado() {
            when(proyectoRepository.findAllWithFrecuenciaControl()).thenReturn(List.of());

            scheduler.cerrarPeriodosVencidos();

            verify(cerrarPeriodoUseCase, never()).cerrar(any(), any());
        }
    }

    @Nested
    @DisplayName("P3: SEMANAL not aligned today")
    class P3NoAlineado {

        @Test
        @DisplayName("cerrar() no llamado, WARN logged")
        void cerrarNoLlamadoWarnLogged() {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            ProyectoId p3Id = ProyectoId.nuevo();
            LocalDateTime fechaInicio = today.minusDays(1).atStartOfDay();
            Proyecto p3 = Proyecto.reconstruir(
                    p3Id, "P3", "Loc", EstadoProyecto.ACTIVO,
                    fechaInicio, FrecuenciaControl.SEMANAL);

            when(proyectoRepository.findAllWithFrecuenciaControl()).thenReturn(List.of(p3));

            scheduler.cerrarPeriodosVencidos();

            verify(cerrarPeriodoUseCase, never()).cerrar(any(), any());
            String formatted = logAppender.list.stream()
                    .filter(e -> e.getLevel() == Level.WARN)
                    .map(ILoggingEvent::getFormattedMessage)
                    .findFirst().orElse("");
            assertThat(formatted).contains("Periodo omitido").contains(p3Id.getValue().toString());
        }
    }

    @Nested
    @DisplayName("P4: MENSUAL aligned, existing evm_time_series row for today")
    class P4DuplicatePrevention {

        @Test
        @DisplayName("cerrar() no llamado por duplicate prevention")
        void cerrarNoLlamadoPorExistente() {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            ProyectoId p4Id = ProyectoId.nuevo();
            Proyecto p4 = Proyecto.reconstruir(
                    p4Id, "P4", "Loc", EstadoProyecto.ACTIVO,
                    today.minusMonths(1).atStartOfDay(), FrecuenciaControl.MENSUAL);

            when(proyectoRepository.findAllWithFrecuenciaControl()).thenReturn(List.of(p4));
            when(evmTimeSeriesRepository.existsByProyectoIdAndFechaCorte(p4Id.getValue(), today)).thenReturn(true);

            scheduler.cerrarPeriodosVencidos();

            verify(cerrarPeriodoUseCase, never()).cerrar(any(), any());
        }
    }

    @Nested
    @DisplayName("P5 exception, P6 still processed")
    class P5FailureP6Continues {

        @Test
        @DisplayName("un proyecto falla, el siguiente se procesa, ERROR logged")
        void unFalloNoBloqueaOtros() {
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            ProyectoId p5Id = ProyectoId.nuevo();
            ProyectoId p6Id = ProyectoId.nuevo();
            Proyecto p5 = Proyecto.reconstruir(
                    p5Id, "P5", "Loc", EstadoProyecto.ACTIVO,
                    today.minusMonths(1).atStartOfDay(), FrecuenciaControl.MENSUAL);
            Proyecto p6 = Proyecto.reconstruir(
                    p6Id, "P6", "Loc", EstadoProyecto.ACTIVO,
                    today.minusMonths(1).atStartOfDay(), FrecuenciaControl.MENSUAL);

            when(proyectoRepository.findAllWithFrecuenciaControl()).thenReturn(List.of(p5, p6));
            when(evmTimeSeriesRepository.existsByProyectoIdAndFechaCorte(any(), eq(today))).thenReturn(false);
            doThrow(new RuntimeException("DB error")).when(cerrarPeriodoUseCase).cerrar(eq(p5Id.getValue()), eq(today));

            scheduler.cerrarPeriodosVencidos();

            verify(cerrarPeriodoUseCase).cerrar(p5Id.getValue(), today);
            verify(cerrarPeriodoUseCase).cerrar(p6Id.getValue(), today);
            String errorFormatted = logAppender.list.stream()
                    .filter(e -> e.getLevel() == Level.ERROR)
                    .map(ILoggingEvent::getFormattedMessage)
                    .findFirst().orElse("");
            assertThat(errorFormatted).contains("Error cerrando periodo").contains(p5Id.getValue().toString());
        }
    }
}
