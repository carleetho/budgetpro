package com.budgetpro.application.finanzas.evm;

import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.application.finanzas.evm.port.in.SCurveResult;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeriesId;
import com.budgetpro.domain.finanzas.evm.port.out.EVMDataProvider;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerSCurveUseCaseImplTest {

    @Mock
    private EVMTimeSeriesRepository evmTimeSeriesRepository;

    @Mock
    private EVMDataProvider evmDataProvider;

    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private ObtenerSCurveUseCaseImpl useCase;

    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
    }

    @Test
    void deberiaRetornarSCurveOrdenadaYConBacAjustadoDelProvider() {
        EVMTimeSeries periodo1 = crearRow(LocalDate.of(2025, 1, 31), 1, new BigDecimal("0.9694"), new BigDecimal("0.9500"));
        EVMTimeSeries periodo2 = crearRowMock(LocalDate.of(2025, 2, 28), 2, new BigDecimal("0.9800"), null);

        when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(true);
        when(evmDataProvider.getAdjustedBudgetAtCompletion(proyectoId)).thenReturn(new BigDecimal("520000.00"));
        when(evmTimeSeriesRepository.findByProyectoId(eq(proyectoId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(
                        periodo2,
                        periodo1));

        SCurveResult result = useCase.obtener(
                proyectoId,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31));

        assertThat(result.proyectoId()).isEqualTo(proyectoId);
        assertThat(result.moneda()).isEqualTo("USD");
        assertThat(result.bacTotal()).isEqualByComparingTo("500000.00");
        assertThat(result.bacAjustado()).isEqualByComparingTo("520000.00");
        assertThat(result.dataPoints()).hasSize(2);
        assertThat(result.dataPoints().get(0).fechaCorte()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(result.dataPoints().get(1).fechaCorte()).isEqualTo(LocalDate.of(2025, 2, 28));
        assertThat(result.dataPoints().get(0).cpiPeriodo()).isEqualByComparingTo("0.9694");
        assertThat(result.dataPoints().get(0).spiPeriodo()).isEqualByComparingTo("0.9500");
        assertThat(result.dataPoints().get(1).spiPeriodo()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(evmDataProvider).getAdjustedBudgetAtCompletion(proyectoId);
        verify(evmDataProvider, never()).getBudgetAtCompletion(proyectoId);
    }

    @Test
    void deberiaRetornarEstadoVacioSiNoHaySerieTemporal() {
        when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(true);
        when(evmTimeSeriesRepository.findByProyectoId(eq(proyectoId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(evmDataProvider.getAdjustedBudgetAtCompletion(proyectoId)).thenReturn(new BigDecimal("520000.00"));
        when(evmDataProvider.getBudgetAtCompletion(proyectoId)).thenReturn(new BigDecimal("500000.00"));

        SCurveResult result = useCase.obtener(
                proyectoId,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31));

        assertThat(result.proyectoId()).isEqualTo(proyectoId);
        assertThat(result.moneda()).isNull();
        assertThat(result.bacTotal()).isEqualByComparingTo("500000.00");
        assertThat(result.bacAjustado()).isEqualByComparingTo("520000.00");
        assertThat(result.dataPoints()).isEmpty();
    }

    @Test
    void deberiaLanzarProyectoNotFoundSiProyectoNoExiste() {
        when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(false);

        assertThatThrownBy(() -> useCase.obtener(proyectoId, LocalDate.now(), LocalDate.now()))
                .isInstanceOf(ProyectoNotFoundException.class)
                .hasMessageContaining(proyectoId.toString());

        verify(evmTimeSeriesRepository, never()).findByProyectoId(any(UUID.class), any(LocalDate.class), any(LocalDate.class));
        verify(evmDataProvider, never()).getAdjustedBudgetAtCompletion(proyectoId);
    }

    @Test
    void deberiaPasarFechasNullDirectamenteAlRepositorio() {
        when(proyectoRepository.existsById(any(ProyectoId.class))).thenReturn(true);
        when(evmTimeSeriesRepository.findByProyectoId(eq(proyectoId), isNull(), isNull())).thenReturn(List.of());
        when(evmDataProvider.getAdjustedBudgetAtCompletion(proyectoId)).thenReturn(new BigDecimal("520000.00"));
        when(evmDataProvider.getBudgetAtCompletion(proyectoId)).thenReturn(new BigDecimal("500000.00"));

        useCase.obtener(proyectoId, null, null);

        verify(evmTimeSeriesRepository).findByProyectoId(proyectoId, null, null);
    }

    private EVMTimeSeries crearRow(LocalDate fechaCorte, int periodo, BigDecimal cpiPeriodo, BigDecimal spiPeriodo) {
        return EVMTimeSeries.reconstruir(
                EVMTimeSeriesId.nuevo(),
                proyectoId,
                fechaCorte,
                periodo,
                new BigDecimal("100000.00"),
                new BigDecimal("95000.00"),
                new BigDecimal("98000.00"),
                new BigDecimal("500000.00"),
                new BigDecimal("520000.00"),
                cpiPeriodo,
                spiPeriodo,
                "USD");
    }

    private EVMTimeSeries crearRowMock(LocalDate fechaCorte, int periodo, BigDecimal cpiPeriodo, BigDecimal spiPeriodo) {
        EVMTimeSeries row = mock(EVMTimeSeries.class);
        when(row.getFechaCorte()).thenReturn(fechaCorte);
        when(row.getPeriodo()).thenReturn(periodo);
        when(row.getPvAcumulado()).thenReturn(new BigDecimal("100000.00"));
        when(row.getEvAcumulado()).thenReturn(new BigDecimal("95000.00"));
        when(row.getAcAcumulado()).thenReturn(new BigDecimal("98000.00"));
        when(row.getCpiPeriodo()).thenReturn(cpiPeriodo);
        when(row.getSpiPeriodo()).thenReturn(spiPeriodo);
        return row;
    }
}
