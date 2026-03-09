package com.budgetpro.application.finanzas.evm.service;

import com.budgetpro.application.finanzas.evm.event.ValuacionCerradaEvent;
import com.budgetpro.application.finanzas.evm.exception.PeriodoFechaInvalidaException;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CerrarPeriodoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CerrarPeriodoService service;
    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        service = new CerrarPeriodoService(proyectoRepository, eventPublisher);
        proyectoId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("cerrar() con fecha alineada")
    class FechaAlineada {

        @Test
        @DisplayName("publica ValuacionCerradaEvent exactamente una vez")
        void publicaEventoUnaVez() {
            LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 6, 0, 0);
            LocalDate fechaCorte = LocalDate.of(2025, 1, 13);
            Proyecto proyecto = Proyecto.reconstruir(
                    ProyectoId.from(proyectoId), "Proyecto", "Loc", EstadoProyecto.ACTIVO,
                    fechaInicio, FrecuenciaControl.SEMANAL);

            when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.of(proyecto));

            service.cerrar(proyectoId, fechaCorte);

            ArgumentCaptor<ValuacionCerradaEvent> captor = ArgumentCaptor.forClass(ValuacionCerradaEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            ValuacionCerradaEvent event = captor.getValue();
            assertThat(event.proyectoId()).isEqualTo(proyectoId);
            assertThat(event.fechaCorte()).isEqualTo(fechaCorte);
            assertThat(event.periodoId()).startsWith("PER-").contains(fechaCorte.toString());
        }
    }

    @Nested
    @DisplayName("cerrar() con fecha desalineada")
    class FechaDesalineada {

        @Test
        @DisplayName("lanza PeriodoFechaInvalidaException con fechaCorte y frecuencia correctos")
        void lanzaExcepcionConCamposCorrectos() {
            LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 6, 0, 0);
            LocalDate fechaCorte = LocalDate.of(2025, 1, 10);
            Proyecto proyecto = Proyecto.reconstruir(
                    ProyectoId.from(proyectoId), "Proyecto", "Loc", EstadoProyecto.ACTIVO,
                    fechaInicio, FrecuenciaControl.SEMANAL);

            when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.of(proyecto));

            assertThatThrownBy(() -> service.cerrar(proyectoId, fechaCorte))
                    .isInstanceOf(PeriodoFechaInvalidaException.class)
                    .satisfies(ex -> {
                        PeriodoFechaInvalidaException pe = (PeriodoFechaInvalidaException) ex;
                        assertThat(pe.getFechaCorte()).isEqualTo(fechaCorte);
                        assertThat(pe.getFrecuencia()).isEqualTo(FrecuenciaControl.SEMANAL);
                        assertThat(pe.getMessage()).contains("SEMANAL");
                    });

            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("cerrar() con proyecto inexistente")
    class ProyectoInexistente {

        @Test
        @DisplayName("lanza ProyectoNotFoundException")
        void lanzaProyectoNotFound() {
            when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cerrar(proyectoId, LocalDate.of(2025, 1, 13)))
                    .isInstanceOf(ProyectoNotFoundException.class)
                    .satisfies(ex -> assertThat(((ProyectoNotFoundException) ex).getProyectoId()).isEqualTo(proyectoId));

            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("cerrar() con frecuenciaControl null")
    class SinFrecuencia {

        @Test
        @DisplayName("sigue adelante y publica evento sin validación")
        void procedeSinValidacion() {
            Proyecto proyecto = Proyecto.reconstruir(
                    ProyectoId.from(proyectoId), "Proyecto", "Loc", EstadoProyecto.ACTIVO,
                    null, null);

            when(proyectoRepository.findById(ProyectoId.from(proyectoId))).thenReturn(Optional.of(proyecto));

            service.cerrar(proyectoId, LocalDate.of(2025, 6, 15));

            ArgumentCaptor<ValuacionCerradaEvent> captor = ArgumentCaptor.forClass(ValuacionCerradaEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().proyectoId()).isEqualTo(proyectoId);
            assertThat(captor.getValue().fechaCorte()).isEqualTo(LocalDate.of(2025, 6, 15));
        }
    }
}
