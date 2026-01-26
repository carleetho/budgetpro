package com.budgetpro.application.apu.usecase;

import com.budgetpro.application.apu.exception.ApuNoEncontradoException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.domain.catalogo.service.CalculoApuDinamicoService;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActualizarRendimientoUseCaseImplTest {

    @Mock
    private ApuSnapshotRepository apuSnapshotRepository;

    @Mock
    private CalculoApuDinamicoService calculoApuDinamicoService;

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private IntegrityHashService integrityHashService;

    @InjectMocks
    private ActualizarRendimientoUseCaseImpl useCase;

    private UUID apuSnapshotId;
    private UUID partidaId;
    private UUID presupuestoId;
    private UUID usuarioId;
    private APUSnapshot apuSnapshot;
    private Partida partida;
    private Presupuesto presupuesto;

    @BeforeEach
    void setUp() {
        apuSnapshotId = UUID.randomUUID();
        partidaId = UUID.randomUUID();
        presupuestoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        // Crear APUSnapshot con rendimiento inicial 25
        apuSnapshot = APUSnapshot.crear(
                APUSnapshotId.of(apuSnapshotId),
                partidaId,
                "APU-001",
                "CATALOG-001",
                new BigDecimal("25.00"),
                "m³",
                LocalDateTime.now()
        );

        // Crear partida
        partida = Partida.crearRaiz(
                PartidaId.from(partidaId),
                presupuestoId,
                "01.01",
                "Partida de prueba",
                "m³",
                new BigDecimal("100.00")
        );

        // Crear presupuesto en edición
        presupuesto = Presupuesto.crear(
                PresupuestoId.from(presupuestoId),
                UUID.randomUUID(),
                "Presupuesto de prueba"
        );
    }

    @Test
    void deberiaActualizarRendimientoEnPresupuestoNoAprobado() {
        // Given
        BigDecimal nuevoRendimiento = new BigDecimal("30.00");
        BigDecimal costoTotalEsperado = new BigDecimal("1000.00");

        when(apuSnapshotRepository.findById(apuSnapshotId)).thenReturn(Optional.of(apuSnapshot));
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.of(presupuesto));
        when(calculoApuDinamicoService.calcularCostoTotalAPU(any(APUSnapshot.class), anyString()))
                .thenReturn(costoTotalEsperado);
        when(apuSnapshotRepository.save(any(APUSnapshot.class))).thenReturn(apuSnapshot);

        // When
        useCase.actualizarRendimiento(apuSnapshotId, nuevoRendimiento, usuarioId);

        // Then
        assertThat(apuSnapshot.getRendimientoVigente()).isEqualByComparingTo(nuevoRendimiento);
        assertThat(apuSnapshot.isRendimientoModificado()).isTrue();
        assertThat(apuSnapshot.getRendimientoModificadoPor()).isEqualTo(usuarioId);
        assertThat(apuSnapshot.getRendimientoModificadoEn()).isNotNull();

        verify(apuSnapshotRepository).save(apuSnapshot);
        verify(calculoApuDinamicoService).calcularCostoTotalAPU(any(APUSnapshot.class), anyString());
        verify(integrityHashService, never()).calculateExecutionHash(any(Presupuesto.class));
    }

    @Test
    void deberiaActualizarRendimientoYHashEjecucionEnPresupuestoAprobado() {
        // Given
        BigDecimal nuevoRendimiento = new BigDecimal("30.00");
        BigDecimal costoTotalEsperado = new BigDecimal("1000.00");
        String hashAprobacion = "hash-aprobacion";
        String hashEjecucion = "hash-ejecucion-actualizado";

        // Configurar mocks antes de aprobar
        when(integrityHashService.calculateApprovalHash(any(Presupuesto.class))).thenReturn(hashAprobacion);
        when(integrityHashService.calculateExecutionHash(any(Presupuesto.class))).thenReturn(hashEjecucion);
        
        // Aprobar presupuesto
        presupuesto.aprobar(usuarioId, integrityHashService);

        when(apuSnapshotRepository.findById(apuSnapshotId)).thenReturn(Optional.of(apuSnapshot));
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.of(presupuesto));
        when(calculoApuDinamicoService.calcularCostoTotalAPU(any(APUSnapshot.class), anyString()))
                .thenReturn(costoTotalEsperado);
        when(apuSnapshotRepository.save(any(APUSnapshot.class))).thenReturn(apuSnapshot);
        doNothing().when(presupuestoRepository).save(any(Presupuesto.class));

        // When
        useCase.actualizarRendimiento(apuSnapshotId, nuevoRendimiento, usuarioId);

        // Then
        assertThat(apuSnapshot.getRendimientoVigente()).isEqualByComparingTo(nuevoRendimiento);
        verify(apuSnapshotRepository).save(apuSnapshot);
        verify(presupuestoRepository).save(presupuesto);
        verify(integrityHashService, times(2)).calculateExecutionHash(presupuesto); // Una vez al aprobar, otra al actualizar
    }

    @Test
    void deberiaRechazarRendimientoInvalido() {
        // When/Then
        assertThatThrownBy(() -> useCase.actualizarRendimiento(apuSnapshotId, BigDecimal.ZERO, usuarioId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El rendimiento debe ser mayor a 0");

        assertThatThrownBy(() -> useCase.actualizarRendimiento(apuSnapshotId, new BigDecimal("-10"), usuarioId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El rendimiento debe ser mayor a 0");

        verify(apuSnapshotRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiApuNoExiste() {
        // Given
        when(apuSnapshotRepository.findById(apuSnapshotId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> useCase.actualizarRendimiento(apuSnapshotId, new BigDecimal("30"), usuarioId))
                .isInstanceOf(ApuNoEncontradoException.class)
                .hasMessageContaining(apuSnapshotId.toString());

        verify(apuSnapshotRepository, never()).save(any());
    }

    @Test
    void deberiaRecalcularEnCascadaHerramientas() {
        // Given
        BigDecimal nuevoRendimiento = new BigDecimal("30.00");
        BigDecimal costoTotalEsperado = new BigDecimal("1000.00");

        // Agregar insumo de MANO_OBRA
        List<com.budgetpro.domain.catalogo.model.ComposicionCuadrillaSnapshot> cuadrilla = List.of(
                new com.budgetpro.domain.catalogo.model.ComposicionCuadrillaSnapshot(
                        "PERS-001", "Operario", new BigDecimal("1.0"), new BigDecimal("80.00"), "PEN")
        );

        APUInsumoSnapshot insumoMO = APUInsumoSnapshot.crear(
                APUInsumoSnapshotId.of(UUID.randomUUID()),
                "REC-MO-001",
                "Operario",
                new BigDecimal("1.0"),
                new BigDecimal("80.00"),
                TipoRecurso.MANO_OBRA,
                1,
                new BigDecimal("1.0"),
                "cuadrilla",
                "HR",
                new BigDecimal("1.0"),
                "HR",
                "PEN",
                BigDecimal.ONE,
                null,
                null,
                null,
                null,
                cuadrilla,
                null,
                8,
                null,
                null,
                new BigDecimal("0.03"),
                null
        );

        apuSnapshot.agregarInsumo(insumoMO);

        when(apuSnapshotRepository.findById(apuSnapshotId)).thenReturn(Optional.of(apuSnapshot));
        when(partidaRepository.findById(partidaId)).thenReturn(Optional.of(partida));
        when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.of(presupuesto));
        when(calculoApuDinamicoService.calcularCostoTotalAPU(any(APUSnapshot.class), anyString()))
                .thenReturn(costoTotalEsperado);
        when(apuSnapshotRepository.save(any(APUSnapshot.class))).thenReturn(apuSnapshot);

        // When
        useCase.actualizarRendimiento(apuSnapshotId, nuevoRendimiento, usuarioId);

        // Then
        verify(calculoApuDinamicoService).calcularCostoTotalAPU(eq(apuSnapshot), anyString());
        // El servicio de cálculo debe manejar el recálculo en cascada de herramientas
        assertThat(apuSnapshot.getRendimientoVigente()).isEqualByComparingTo(nuevoRendimiento);
    }
}
