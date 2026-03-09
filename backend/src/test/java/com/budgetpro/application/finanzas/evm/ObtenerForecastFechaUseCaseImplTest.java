package com.budgetpro.application.finanzas.evm;

import com.budgetpro.application.finanzas.evm.port.in.ForecastResult;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeriesId;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.finanzas.evm.util.WorkingDayCalculator;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerForecastFechaUseCaseImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository programaObraRepository;

    @Mock
    private EVMTimeSeriesRepository evmTimeSeriesRepository;

    @Mock
    private WorkingDayCalculator workingDayCalculator;

    private ObtenerForecastFechaUseCaseImpl useCase;
    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        useCase = new ObtenerForecastFechaUseCaseImpl(
                proyectoRepository,
                programaObraRepository,
                evmTimeSeriesRepository,
                workingDayCalculator);
        proyectoId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("AC-E05-01: Normal forecast")
    class NormalForecast {

        @Test
        @DisplayName("calcula spiUsed=0.8500, forecastFallback=false y forecastCompletionDate con formula plusWorkingDays")
        void deberiaCalcularForecastNormal() {
            LocalDate fechaCorte = LocalDate.of(2025, 6, 15);
            LocalDate fechaFinPlanificada = LocalDate.of(2025, 12, 31);
            LocalDate expectedForecastDate = LocalDate.of(2026, 4, 10);

            EVMTimeSeries ts = crearEVMTimeSeries(fechaCorte, new BigDecimal("85000"), new BigDecimal("100000"));
            ProgramaObra programa = ProgramaObra.reconstruir(
                    ProgramaObraId.nuevo(),
                    proyectoId,
                    LocalDate.of(2025, 1, 1),
                    fechaFinPlanificada,
                    365,
                    0L);

            when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(true);
            when(programaObraRepository.findByProyectoId(proyectoId)).thenReturn(Optional.of(programa));
            when(evmTimeSeriesRepository.findLatestByProyectoId(proyectoId)).thenReturn(Optional.of(ts));
            when(workingDayCalculator.workingDaysBetween(eq(fechaCorte), eq(fechaFinPlanificada))).thenReturn(200);
            when(workingDayCalculator.plusWorkingDays(eq(fechaCorte), eq(236))).thenReturn(expectedForecastDate);

            ForecastResult result = useCase.obtener(proyectoId);

            assertThat(result.proyectoId()).isEqualTo(proyectoId);
            assertThat(result.fechaCorteBase()).isEqualTo(fechaCorte);
            assertThat(result.forecastCompletionDate()).isEqualTo(expectedForecastDate);
            assertThat(result.fechaFinPlanificada()).isEqualTo(fechaFinPlanificada);
            assertThat(result.remainingDays()).isEqualTo(200);
            assertThat(result.spiUsed()).isEqualByComparingTo("0.8500");
            assertThat(result.forecastFallback()).isFalse();

            verify(workingDayCalculator).workingDaysBetween(fechaCorte, fechaFinPlanificada);
            verify(workingDayCalculator).plusWorkingDays(fechaCorte, 236);
        }
    }

    @Nested
    @DisplayName("AC-E05-02: SPI == 0 fallback")
    class SpiZeroFallback {

        @Test
        @DisplayName("retorna forecastCompletionDate=fechaFinPlanificada, forecastFallback=true, spiUsed=0.0000")
        void deberiaAplicarFallbackCuandoPvAcumuladoEsCero() {
            LocalDate fechaCorte = LocalDate.of(2025, 6, 15);
            LocalDate fechaFinPlanificada = LocalDate.of(2025, 12, 31);

            EVMTimeSeries ts = crearEVMTimeSeries(fechaCorte, new BigDecimal("50000"), BigDecimal.ZERO);
            ProgramaObra programa = ProgramaObra.reconstruir(
                    ProgramaObraId.nuevo(),
                    proyectoId,
                    LocalDate.of(2025, 1, 1),
                    fechaFinPlanificada,
                    365,
                    0L);

            when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(true);
            when(programaObraRepository.findByProyectoId(proyectoId)).thenReturn(Optional.of(programa));
            when(evmTimeSeriesRepository.findLatestByProyectoId(proyectoId)).thenReturn(Optional.of(ts));

            ForecastResult result = useCase.obtener(proyectoId);

            assertThat(result.forecastCompletionDate()).isEqualTo(fechaFinPlanificada);
            assertThat(result.forecastFallback()).isTrue();
            assertThat(result.spiUsed()).isEqualByComparingTo("0.0000");
            assertThat(result.fechaCorteBase()).isEqualTo(fechaCorte);
            assertThat(result.remainingDays()).isEqualTo(0);

            verify(workingDayCalculator, never()).workingDaysBetween(any(), any());
            verify(workingDayCalculator, never()).plusWorkingDays(any(), anyInt());
        }
    }

    @Nested
    @DisplayName("AC-E05-03: No time-series data fallback")
    class NoTimeSeriesFallback {

        @Test
        @DisplayName("retorna fechaCorteBase=null, remainingDays=0, forecastFallback=true")
        void deberiaAplicarFallbackCuandoNoHayDatosDeSerieTemporal() {
            LocalDate fechaFinPlanificada = LocalDate.of(2025, 12, 31);
            ProgramaObra programa = ProgramaObra.reconstruir(
                    ProgramaObraId.nuevo(),
                    proyectoId,
                    LocalDate.of(2025, 1, 1),
                    fechaFinPlanificada,
                    365,
                    0L);

            when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(true);
            when(programaObraRepository.findByProyectoId(proyectoId)).thenReturn(Optional.of(programa));
            when(evmTimeSeriesRepository.findLatestByProyectoId(proyectoId)).thenReturn(Optional.empty());

            ForecastResult result = useCase.obtener(proyectoId);

            assertThat(result.fechaCorteBase()).isNull();
            assertThat(result.remainingDays()).isEqualTo(0);
            assertThat(result.forecastFallback()).isTrue();
            assertThat(result.forecastCompletionDate()).isEqualTo(fechaFinPlanificada);
            assertThat(result.spiUsed()).isEqualByComparingTo("0.0000");

            verify(workingDayCalculator, never()).workingDaysBetween(any(), any());
            verify(workingDayCalculator, never()).plusWorkingDays(any(), anyInt());
        }
    }

    @Nested
    @DisplayName("AC-E05-04: Working days exclude weekends")
    class WorkingDaysExcludeWeekends {

        @Test
        @DisplayName("remainingDays=5 para fechaCorte=2025-01-31 (viernes) a fechaFinPlanificada=2025-02-07 (viernes)")
        void deberiaContarSoloDiasLaborablesExcluyendoFinesDeSemana() {
            LocalDate fechaCorte = LocalDate.of(2025, 1, 31);
            LocalDate fechaFinPlanificada = LocalDate.of(2025, 2, 7);

            WorkingDayCalculator realCalculator = new WorkingDayCalculator();
            useCase = new ObtenerForecastFechaUseCaseImpl(
                    proyectoRepository,
                    programaObraRepository,
                    evmTimeSeriesRepository,
                    realCalculator);

            EVMTimeSeries ts = crearEVMTimeSeries(fechaCorte, new BigDecimal("85000"), new BigDecimal("100000"));
            ProgramaObra programa = ProgramaObra.reconstruir(
                    ProgramaObraId.nuevo(),
                    proyectoId,
                    LocalDate.of(2025, 1, 1),
                    fechaFinPlanificada,
                    7,
                    0L);

            when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(true);
            when(programaObraRepository.findByProyectoId(proyectoId)).thenReturn(Optional.of(programa));
            when(evmTimeSeriesRepository.findLatestByProyectoId(proyectoId)).thenReturn(Optional.of(ts));

            ForecastResult result = useCase.obtener(proyectoId);

            assertThat(result.remainingDays()).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("lanza ProyectoNotFoundException cuando proyecto no existe")
    void deberiaLanzarProyectoNotFoundCuandoProyectoNoExiste() {
        when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(false);

        assertThatThrownBy(() -> useCase.obtener(proyectoId))
                .isInstanceOf(ProyectoNotFoundException.class)
                .hasMessageContaining(proyectoId.toString());

        verify(programaObraRepository, never()).findByProyectoId(any());
        verify(evmTimeSeriesRepository, never()).findLatestByProyectoId(any());
    }

    private EVMTimeSeries crearEVMTimeSeries(LocalDate fechaCorte, BigDecimal evAcumulado, BigDecimal pvAcumulado) {
        return EVMTimeSeries.reconstruir(
                EVMTimeSeriesId.nuevo(),
                proyectoId,
                fechaCorte,
                1,
                pvAcumulado,
                evAcumulado,
                new BigDecimal("80000"),
                new BigDecimal("500000"),
                new BigDecimal("520000"),
                new BigDecimal("0.95"),
                new BigDecimal("0.85"),
                "USD");
    }
}
